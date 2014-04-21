package com.atlassian.core.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;

public class ExpiresFilter extends AbstractHttpFilter
{
    private int expiryTimeInSeconds = 0;

    public void init(FilterConfig filterConfig) throws ServletException
    {
        super.init(filterConfig);
        String str = filterConfig.getInitParameter("expiryTimeInSeconds");
        if (str != null)
        {
            try
            {
                expiryTimeInSeconds = Integer.parseInt(str);
            }
            catch (NumberFormatException nfe)
            {
                throw new ServletException("'" + str + "' is not a valid integer.", nfe);
            }
        }
        if (System.getProperty("atlassian.disable.caches","false").equals("true"))
        {
            expiryTimeInSeconds = 0; // don't add expires headers
        }
    }

    public void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException
    {
        if (expiryTimeInSeconds > 0)
        {
            response.setDateHeader("Expires", new Date().getTime() + expiryTimeInSeconds * 1000);
        }
        filterChain.doFilter(request, response);
    }
}
