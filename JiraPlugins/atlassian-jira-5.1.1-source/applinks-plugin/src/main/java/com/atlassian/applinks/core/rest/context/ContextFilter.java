package com.atlassian.applinks.core.rest.context;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Servlet filter to allow capturing the HttpServletRequest context for the current thread. Useful for services used
 * by REST end-points that need a reference to the HttpServletRequest object
 *
 * @since 3.0
 */
public class ContextFilter implements Filter
{
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException
    {
        final HttpServletRequest previousValue = CurrentContext.getHttpServletRequest();
        try {
            CurrentContext.setHttpServletRequest((HttpServletRequest) servletRequest);
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            CurrentContext.setHttpServletRequest(previousValue);
        }
    }

    public void init(final FilterConfig filterConfig) throws ServletException
    {
        // no-op
    }

    public void destroy()
    {
        // no-op
    }
}
