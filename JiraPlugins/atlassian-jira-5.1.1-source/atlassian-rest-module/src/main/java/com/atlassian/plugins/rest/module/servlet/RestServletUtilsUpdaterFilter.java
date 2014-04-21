package com.atlassian.plugins.rest.module.servlet;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class RestServletUtilsUpdaterFilter implements Filter
{

    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        // This sucks, I hate relying on ThreadLocals but we have to do that in order to use the SAL UserManager API. 
        ServletUtils.setHttpServletRequest(request);
        try
        {
            filterChain.doFilter(request, response);
        }
        finally
        {
            ServletUtils.setHttpServletRequest(null);
        }
    }

    /**
     * Simply calls {@link #doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)}
     * @see #doFilterInternal(HttpServletRequest, HttpServletResponse, FilterChain)
     */
    public final void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
    {
        doFilterInternal((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    public void destroy()
    {
    }
}
