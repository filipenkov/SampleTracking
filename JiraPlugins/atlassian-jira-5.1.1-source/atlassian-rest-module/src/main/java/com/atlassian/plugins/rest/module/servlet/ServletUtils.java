package com.atlassian.plugins.rest.module.servlet;

import javax.servlet.http.HttpServletRequest;

/**
 * A class to hold some servlet related information.
 */
public class ServletUtils
{
    private static final ThreadLocal<HttpServletRequest> HTTP_SERVLET_REQUEST_THREAD_LOCAL = new ThreadLocal<HttpServletRequest>();

    public static HttpServletRequest getHttpServletRequest()
    {
        return HTTP_SERVLET_REQUEST_THREAD_LOCAL.get();
    }

    public static void setHttpServletRequest(final HttpServletRequest request)
    {
        HTTP_SERVLET_REQUEST_THREAD_LOCAL.set(request);
    }
}
