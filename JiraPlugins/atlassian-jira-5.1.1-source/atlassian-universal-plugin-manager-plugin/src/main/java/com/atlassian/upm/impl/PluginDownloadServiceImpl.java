package com.atlassian.upm.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;

import com.atlassian.plugins.client.service.ClientContextFactory;
import com.atlassian.plugins.client.service.HttpClientFactory;
import com.atlassian.plugins.service.ClientContext;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.upm.AccessDeniedException;
import com.atlassian.upm.PluginDownloadService;
import com.atlassian.upm.RelativeURIException;
import com.atlassian.upm.UnsupportedProtocolException;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.atlassian.upm.api.util.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;
import static javax.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static javax.ws.rs.core.HttpHeaders.CONTENT_TYPE;
import static javax.ws.rs.core.Response.Status.MOVED_PERMANENTLY;
import static javax.ws.rs.core.Response.Status.OK;
import static javax.ws.rs.core.Response.Status.SEE_OTHER;
import static javax.ws.rs.core.Response.Status.TEMPORARY_REDIRECT;
import static javax.ws.rs.core.Response.Status.UNAUTHORIZED;
import static org.apache.commons.io.FileUtils.openOutputStream;
import static org.apache.commons.io.IOUtils.closeQuietly;

/**
 * Implementation of {@code PluginDownloadService} that uses SAL's {@link Request} to download the plugin file.
 */
final class PluginDownloadServiceImpl implements PluginDownloadService
{
    private static final Logger log = LoggerFactory.getLogger(PluginDownloadServiceImpl.class);
    private static final String[] ACCEPTABLE_MEDIA_TYPES = new String[]{
        MediaType.APPLICATION_OCTET_STREAM,
        "application/java-archive",
        MediaType.WILDCARD
    };
    private static final int DOWNLOAD_BUFFER_SIZE = 1024 * 4;
    private static final Predicate<Integer> REDIRECT = Predicates.in(ImmutableSet.of(
        TEMPORARY_REDIRECT.getStatusCode(),
        MOVED_PERMANENTLY.getStatusCode(),
        SEE_OTHER.getStatusCode(),
        302 // Found
    ));
    private static final int MAX_FOLLOW_REDIRECTS = 3;
    private static final String URI_HEADER = "__uri__";

    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";
    private static final String CONTENT_DISPOSITION = "Content-Disposition";
    private static final Pattern CONTENT_DISPOSITION_FILENAME_REGEX = Pattern.compile("filename=\"([^\"]*)\"");
    
    private final ClientContextFactory clientContextFactory;

    public PluginDownloadServiceImpl(ClientContextFactory clientContextFactory)
    {
        this.clientContextFactory = checkNotNull(clientContextFactory, "clientContextFactory");
    }

    public File downloadPlugin(final URI uri, final String username, final String password, final ProgressTracker progressTracker) throws ResponseException
    {
        log.info("Downloading plugin artifact from [" + uri + "], with username [" + username + "]...");

        // UPM-964 : Check for unsupported URI scheme's before downloading
        checkIfURIIsSupported(uri);

        final HttpClient client = HttpClientFactory.createHttpClient();
        client.getHttpConnectionManager().getParams().setConnectionTimeout(30000);
        client.getHttpConnectionManager().getParams().setSoTimeout(30000);
        GetMethod method = null;

        try
        {
            method = get(client, uri, progressTracker, true, MAX_FOLLOW_REDIRECTS);
            return copyToFile(method, progressTracker);
        }
        catch (IOException ioe)
        {
            throw new ResponseException(ioe.getMessage(), ioe);
        }
        finally
        {
            if (method != null)
            {
                method.releaseConnection();
            }
        }
    }

    private GetMethod get(HttpClient client, URI uri, ProgressTracker progressTracker, boolean firstRequest, int followRedirects) throws ResponseException
    {
        GetMethod method = new GetMethod(uri.toString());

        // only send the client context information on the first request, which will be to PAC;
        // don't send it for any subsequent redirects to external sites.
        if (firstRequest)
        {
            method.addRequestHeader(ClientContext.CLIENT_CONTEXT_HEADER, clientContextFactory.getClientContext().toString());
        }

        method.addRequestHeader("Accept", StringUtils.join(ACCEPTABLE_MEDIA_TYPES, ", "));
        // Redirects are handled manually because we won't know the final name of the artifact being downloaded otherwise.
        method.setFollowRedirects(false);
        try
        {
            client.executeMethod(method);
        }
        catch(IOException ioe)
        {
            throw new ResponseException(ioe.getMessage(), ioe);
        }
        if (isRedirect(method))
        {                // if this is a redirection, the connection is no longer needed as we're about to request from the new location.
            method.releaseConnection();

            if (followRedirects > 0)
            {
                URI newUri = URI.create(method.getResponseHeader("Location").getValue());

                // UPM-964 : Check this again here in case original URI redirects to an unsupported protocol
                checkIfURIIsSupported(uri);
                progressTracker.redirectedTo(newUri);
                return get(client, newUri, progressTracker, false, followRedirects - 1);
            }
            else
            {
                throw new ResponseException("Maximum number of redirects reached");
            }
        }
        else if (method.getStatusCode() == UNAUTHORIZED.getStatusCode())
        {
            throw new AccessDeniedException("Requires Authorization.");
        }
        else if (method.getStatusCode() != OK.getStatusCode())
        {
            String responseBody = "";
            try
            {
                responseBody = method.getResponseBodyAsString();
            }
            catch(IOException e)
            {
                // means the body cannot be retrieved.
            }
            throw new ResponseException("Failed to download plugin: " + responseBody);
        }
        else
        {
            method.getParams().setParameter(URI_HEADER, uri);
            return method;
        }
    }

    private boolean isRedirect(HttpMethod method)
    {
        return REDIRECT.apply(method.getStatusCode());
    }

    private File copyToFile(HttpMethod method, ProgressTracker progressTracker)
        throws IOException, ResponseException
    {
        Header contentLengthHeader = method.getResponseHeader(CONTENT_LENGTH);
        Long contentLength = null;
        if (contentLengthHeader != null)
        {
            contentLength = Long.parseLong(contentLengthHeader.getValue());
        }

        String fileName = coerceFileExtensionForContentType(getFileNameFromResponse(method), method);
        File file = File.createTempFile("plugin.", "." + fileName);
        
        InputStream in = null;
        FileOutputStream out = null;
        try
        {
            in = method.getResponseBodyAsStream();
            out = openOutputStream(file);
            copy(in, out, contentLength, fileName, progressTracker);
            return file;
        }
        finally
        {
            closeQuietly(in);
            closeQuietly(out);
        }
    }

    private String getFileNameFromResponse(HttpMethod method)
    {
        Header contentDispositionHeader = method.getResponseHeader(CONTENT_DISPOSITION);
        if (contentDispositionHeader != null)
        {
            Matcher filenameMatcher = CONTENT_DISPOSITION_FILENAME_REGEX.matcher(contentDispositionHeader.getValue());
            if (filenameMatcher.find())
            {
                return filenameMatcher.group(1);
            }
        }
        URI uri = (URI) method.getParams().getParameter(URI_HEADER);
        String path = uri.getPath();
        return path.substring(path.lastIndexOf('/') + 1);                        
    }
    
    private String coerceFileExtensionForContentType(String filename, HttpMethod method)
    {
        // If the response specifies Content-Type, make sure that the filename has an appropriate
        // extension for the content types we care about (jars and XML)-- since the installation
        // logic is going to infer the content type from the extension.
        Header contentTypeHeader = method.getResponseHeader(CONTENT_TYPE);
        if (contentTypeHeader != null)
        {
            String contentType = contentTypeHeader.getValue().toLowerCase();
            if (contentType.contains(";"))
            {
                contentType = contentType.substring(0, contentType.indexOf(";"));
            }
            if (contentType.equals("application/java-archive"))
            {
                return coerceFileExtension(filename, "jar");
            }
            else if (contentType.endsWith("/xml"))
            {
                return coerceFileExtension(filename, "xml");
            }
        }
        return filename;
    }
    
    private String coerceFileExtension(String filename, String extension)
    {
        if (filename.contains("."))
        {
            return filename.substring(0, filename.lastIndexOf(".") + 1) + extension;
        }
        return filename + "." + extension;
    }

    private void copy(InputStream in, FileOutputStream out, Long totalSize, String fileName, ProgressTracker progressTracker) throws IOException
    {
        byte[] buffer = new byte[DOWNLOAD_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = in.read(buffer)))
        {
            out.write(buffer, 0, n);
            count += n;
            progressTracker.notify(new Progress(count, totalSize, some(fileName)));
        }
    }

    private static void checkIfURIIsSupported(URI uri) throws ResponseException
    {
        if (!uri.isAbsolute())
        {
            throw new RelativeURIException("URI must be absolute");
        }

        String scheme = uri.getScheme();

        if (!(scheme.equalsIgnoreCase(HTTP_SCHEME) || scheme.equalsIgnoreCase(HTTPS_SCHEME)))
        {
            throw new UnsupportedProtocolException("URI scheme '" + scheme + "' is not supported");
        }
    }
}
