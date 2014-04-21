package com.atlassian.security.auth.trustedapps;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.URI;
import org.apache.commons.httpclient.URIException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Take a URL and produce an {@link Application}.
 *
 * For production use, only http is really supported. Other URI schemes (i.e. file:///) are used solely
 * for func testing and have little/no error handling code around them. They should not be used in production.
 */
public class URLApplicationRetriever implements ApplicationRetriever
{
    private final String baseUrl;
    private final EncryptionProvider encryptionProvider;

    public URLApplicationRetriever(String baseUrl, EncryptionProvider encryptionProvider)
    {
        Null.not("baseUrl", baseUrl);
        Null.not("encryptionProvider", encryptionProvider);

        this.baseUrl = baseUrl;
        this.encryptionProvider = encryptionProvider;
    }

    public Application getApplication() throws RetrievalException
    {
        final String certUrl = baseUrl + TrustedApplicationUtils.Constant.CERTIFICATE_URL_PATH;
        final URI uri;
        try
        {
            uri = new URI(certUrl, false);
        }
        catch (URIException e)
        {
            throw new RemoteSystemNotFoundException(e);
        }

        final String scheme = uri.getScheme();
        if (scheme == null)
        {
            throw new RemoteSystemNotFoundException(new MalformedURLException("Undefined URI scheme: " + uri));
        }

        // For HTTP (what we actually use in production) we do it a better way
        else if ("http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme))
        {
            return getHttpApplication(certUrl);
        }
        // but we fall back to raw (crappy) URL to allow file:/// primarily for use in func testing.
        else
        {
            return getURLApplication(certUrl);
        }
    }

    private Application getURLApplication(final String certUrl) throws RetrievalException
    {
        try
        {
            final URLConnection con = new URL(certUrl).openConnection();
            con.connect();
            final InputStream in = con.getInputStream();
            try
            {
                final InputStreamReader reader = new InputStreamReader(in);
                final ReaderApplicationRetriever retriever = new ReaderApplicationRetriever(reader, encryptionProvider);
                return retriever.getApplication();
            }
            finally
            {
                closeQuietly(in);
            }
        }
        catch (MalformedURLException e)
        {
            throw new RemoteSystemNotFoundException(e);
        }
        catch (IOException e)
        {
            throw new RemoteSystemNotFoundException(e);
        }
    }

    private Application getHttpApplication(final String certUrl) throws RetrievalException
    {
        try
        {
            final HttpMethod get = new GetMethod(certUrl);
            get.setFollowRedirects(true);
            final HttpClient client = new HttpClient();
            configureHttpClient(client);

            int responseCode  = client.executeMethod(get);
            if (responseCode >= 300)
            {
                throw new ApplicationNotFoundException("Invalid response code of " + responseCode + " returned from: " + certUrl);
            }
            final InputStream in = get.getResponseBodyAsStream();
            try {
                return new InputStreamApplicationRetriever(in, encryptionProvider).getApplication();
            }
            finally
            {
                closeQuietly(in);
            }
        }
        catch (FileNotFoundException e)
        {
            throw new ApplicationNotFoundException(e);
        }
        catch (MalformedURLException e)
        {
            throw new RemoteSystemNotFoundException(e);
        }
        catch (IOException e)
        {
            throw new RemoteSystemNotFoundException(e);
        }
    }

    private void configureHttpClient(HttpClient client)
    {
        //The default time to wait without retrieving data from the remote connection
        final int socketTimeout = Integer.parseInt(System.getProperty("http.socketTimeout", "10000"));
        //The default time allowed for establishing a connection
        final int connectionTimeout = Integer.parseInt(System.getProperty("http.connectionTimeout", "10000"));

        final HttpConnectionManagerParams params = client.getHttpConnectionManager().getParams();
        params.setConnectionTimeout(connectionTimeout);
        params.setSoTimeout(socketTimeout);
    }

    private void closeQuietly(Closeable closeable)
    {
        if (closeable != null)
        {
            try
            {
                closeable.close();
            }
            catch (IOException e)
            {
                // ignore
            }
        }
    }
}