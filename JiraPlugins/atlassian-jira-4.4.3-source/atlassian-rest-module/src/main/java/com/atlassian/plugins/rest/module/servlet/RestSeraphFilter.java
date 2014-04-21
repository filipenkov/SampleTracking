package com.atlassian.plugins.rest.module.servlet;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * This filter needs to run before Seraph's SecurityFilter. It will set a request attribute that tells Seraph
 * "this request is on REST so use a default os_authType of 'any'". For more details look at the
 * https://extranet.atlassian.com/display/DEV/Rest+Authentication+Specification+Proposal
 * @since v2.1
 */
public class RestSeraphFilter implements Filter
{
    // This *must* be the same thing as in com.atlassian.seraph.auth.AuthType
    public static final String DEFAULT_ATTRIBUTE = "os_authTypeDefault";

    public void init(final FilterConfig filterConfig) throws ServletException
    {
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        final HttpServletRequest httpRequest = (HttpServletRequest) request;
        final HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (httpRequest.getServletPath().startsWith("/rest/"))
        {
            // Only set it if someone before us hasn't already done so.
            if (httpRequest.getAttribute(DEFAULT_ATTRIBUTE) == null)
            {
                httpRequest.setAttribute(DEFAULT_ATTRIBUTE, "any");
            }
            chain.doFilter(request, httpResponse);
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void destroy()
    {
    }
}
