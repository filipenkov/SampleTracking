package com.atlassian.applinks.core.auth.oauth;

import java.net.URI;
import java.net.URISyntaxException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @since 3.0
 */
public class RequestUtil
{
    private static final int HTTP_DEFAULT_PORT = 80;
    private static final int HTTPS_DEFAULT_PORT = 443;
    private static final String HTTP_SCHEME = "http";
    private static final String HTTPS_SCHEME = "https";

    /**
     * Returns the base url for a given request.
     *
     * @param request    the original http request.
     * @param baseUrl    the base URL to return if it fails to build the base URL from the request.
     *
     * @return           the base url of this request.
     */
    public static final URI getBaseURLFromRequest(HttpServletRequest request, final URI baseUrl)
    {
        try
        {
            final StringBuilder urlBuilder = new StringBuilder();
            final String scheme = request.getScheme();
            urlBuilder.append(scheme);
            urlBuilder.append("://");
            urlBuilder.append(request.getServerName());
            final int port = request.getServerPort();
            if (!isStandardPort(scheme, port))
            {
                urlBuilder.append(":");
                urlBuilder.append(port);
            }
            urlBuilder.append(request.getContextPath());
            return new URI(urlBuilder.toString());
        }
        catch (Exception ex)
        {
            return baseUrl;
        }
    }

    private static boolean isStandardPort(String scheme, int port)
    {
        if (scheme.equalsIgnoreCase(HTTP_SCHEME) && port == HTTP_DEFAULT_PORT)
        {
            return true;
        }
        if (scheme.equalsIgnoreCase(HTTPS_SCHEME) && port == HTTPS_DEFAULT_PORT)
        {
            return true;
        }
        return false;
    }

}
