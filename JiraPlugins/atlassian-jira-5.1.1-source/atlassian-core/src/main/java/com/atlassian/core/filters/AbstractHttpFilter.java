package com.atlassian.core.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Provides default implementations of {@link #init(FilterConfig)} and {@link #destroy()},
 * and a doFilter method that casts to HttpServletRequest and HttpServletResponse.
 *
 * @since 4.0
 */
public abstract class AbstractHttpFilter implements Filter
{
    /**
     * Default implementation which does nothing.
     */
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    /**
     * If this is an HTTP request, delegates to {@link #doFilter(HttpServletRequest, HttpServletResponse, FilterChain)}.
     * Does nothing if the request and response are not of the HTTP variety.
     */
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        if (request instanceof HttpServletRequest && response instanceof HttpServletResponse)
        {
            doFilter((HttpServletRequest) request, (HttpServletResponse) response, filterChain);
            return;
        }
        filterChain.doFilter(request, response);
    }

    /**
     * @see Filter#doFilter(ServletRequest, ServletResponse, FilterChain)
     */
    protected abstract void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException;

    /**
     * Default implementation which does nothing.
     */
    public void destroy()
    {
    }
}
