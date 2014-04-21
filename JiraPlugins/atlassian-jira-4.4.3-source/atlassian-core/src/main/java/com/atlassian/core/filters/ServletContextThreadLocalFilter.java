package com.atlassian.core.filters;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ServletContextThreadLocalFilter extends AbstractHttpFilter
{
    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        try
        {
            ServletContextThreadLocal.setRequest(request);
            ServletContextThreadLocal.setResponse(response);

            filterChain.doFilter(request, response);
        }
        finally
        {
            ServletContextThreadLocal.setRequest(null);
            ServletContextThreadLocal.setResponse(null);
        }
    }
}
