package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.XsrfCheckFailedException;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import javax.ws.rs.core.MediaType;

/**
 * A filter that filters requests that need XSRF protection.
 * <p/>
 * For now it only checks to see if there is a header.  Later when SAL has XSRF token support, maybe we can implement
 * token checking.
 *
 * @since 2.4
 */
public class XsrfResourceFilter implements ResourceFilter, ContainerRequestFilter
{
    public static final String TOKEN_HEADER = "X-Atlassian-Token";
    public static final String NO_CHECK = "nocheck";

    private static final Set<String> XSRFABLE_TYPES = new HashSet<String>(Arrays.asList(
            MediaType.APPLICATION_FORM_URLENCODED, MediaType.MULTIPART_FORM_DATA, MediaType.TEXT_PLAIN
    ));

    public ContainerRequest filter(final ContainerRequest request)
    {
        if (isXsrfable(request))
        {
            String header = request.getHeaderValue(TOKEN_HEADER);
            if (header == null || !header.toLowerCase(Locale.ENGLISH).equals(NO_CHECK))
            {
                throw new XsrfCheckFailedException();
            }
        }
        return request;
    }

    private boolean isXsrfable(ContainerRequest request)
    {
        String method = request.getMethod();
        return method.equals("GET") || (method.equals("POST") && XSRFABLE_TYPES.contains(mediaTypeToString(request.getMediaType())));
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return this;
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }

    private static String mediaTypeToString(MediaType mediaType)
    {
        return mediaType.getType().toLowerCase(Locale.ENGLISH) + "/" + mediaType.getSubtype().toLowerCase(Locale.ENGLISH);
    }
}
