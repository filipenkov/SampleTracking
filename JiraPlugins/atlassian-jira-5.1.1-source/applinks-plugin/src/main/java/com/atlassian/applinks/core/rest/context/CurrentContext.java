package com.atlassian.applinks.core.rest.context;

import com.sun.jersey.api.core.HttpContext;

import javax.servlet.http.HttpServletRequest;

/**
 * Stores the {@link HttpContext} and {@link HttpServletRequest} for the currently executing request
 */
public class CurrentContext
{
    private static final ThreadLocal<HttpContext> context = new ThreadLocal<HttpContext>();
    private static final ThreadLocal<HttpServletRequest> request = new ThreadLocal<HttpServletRequest>();

    public static HttpContext getContext()
    {
        return context.get();
    }

    public static void setContext(final HttpContext httpContext)
    {
        context.set(httpContext);
    }

    public static HttpServletRequest getHttpServletRequest()
    {
        return request.get();
    }

    public static void setHttpServletRequest(final HttpServletRequest httpServletRequest)
    {
        request.set(httpServletRequest);
    }
}

