package com.atlassian.plugins.rest.common.filter;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import javax.ws.rs.core.HttpHeaders;
import static javax.ws.rs.core.MediaType.*;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.net.URI;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * <p>A filter to handle URI with extensions. It will set the correct accept header for each extension and remove the extension
 * from the URI to allow for normal processing of the request.</p>
 * <p>Currently supported extension and their matching headers are defined in {@link #EXTENSION_TO_ACCEPT_HEADER the extension to header mapping}.</p>
 * <p>One can exclude URIs from being processed by this filter. Simply create the filter with a collection of {@link Pattern patterns} to be excluded.</p>
 * <p><strong>Example:</strong> URI <code>http://localhost:8080/app/rest/my/resource.json</code> would be automatically mapped to URI <code>http://localhost:8080/app/rest/my/resource</code>
 * with its <code>accept</code> header set to <code>application/json</code></p>
 */
@Provider
public class ExtensionJerseyFilter implements ContainerRequestFilter
{
    private static final String DOT = ".";

    private final Collection<Pattern> pathExcludePatterns;

    public ExtensionJerseyFilter()
    {
        pathExcludePatterns = new LinkedList<Pattern>();
    }

    public ExtensionJerseyFilter(Collection<String> pathExcludePatterns)
    {
        Validate.notNull(pathExcludePatterns);
        this.pathExcludePatterns = compilePatterns(pathExcludePatterns);
    }

    final static Map<String, String> EXTENSION_TO_ACCEPT_HEADER = new ImmutableMap.Builder<String, String>()
            .put("txt", TEXT_PLAIN)
            .put("htm", TEXT_HTML)
            .put("html", TEXT_HTML)
            .put("json", APPLICATION_JSON)
            .put("xml", APPLICATION_XML)
            .put("atom", APPLICATION_ATOM_XML)
            .build();

    public ContainerRequest filter(ContainerRequest request)
    {
        // the path to the request without query params
        final String absolutePath = request.getAbsolutePath().toString();

        final String extension = StringUtils.substringAfterLast(absolutePath, DOT);
        if (shouldFilter("/" + StringUtils.difference(request.getBaseUri().toString(), absolutePath), extension))
        {
            request.getRequestHeaders().putSingle(HttpHeaders.ACCEPT, EXTENSION_TO_ACCEPT_HEADER.get(extension));
            final String absolutePathWithoutExtension = StringUtils.substringBeforeLast(absolutePath, DOT);
            request.setUris(request.getBaseUri(), getRequestUri(absolutePathWithoutExtension, request.getQueryParameters()));
        }
        return request;
    }

    private boolean shouldFilter(String restPath, String extension)
    {
        for (Pattern pattern : pathExcludePatterns)
        {
            if (pattern.matcher(restPath).matches())
            {
                return false;
            }
        }
        return EXTENSION_TO_ACCEPT_HEADER.containsKey(extension);
    }

    private URI getRequestUri(String absolutePathWithoutExtension, Map<String, List<String>> queryParams)
    {
        final UriBuilder requestUriBuilder = UriBuilder.fromPath(absolutePathWithoutExtension);
        for (Map.Entry<String, List<String>> queryParamEntry : queryParams.entrySet())
        {
            for (String value : queryParamEntry.getValue())
            {
                requestUriBuilder.queryParam(queryParamEntry.getKey(), value);
            }
        }
        return requestUriBuilder.build();
    }

    private Collection<Pattern> compilePatterns(Collection<String> pathExcludePatterns)
    {
        final Collection<Pattern> patterns = new LinkedList<Pattern>();
        for (String pattern : pathExcludePatterns)
        {
            patterns.add(Pattern.compile(pattern));
        }
        return patterns;
    }
}
