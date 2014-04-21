package com.atlassian.applinks.ui.auth;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * @since 3.0
 */
public class AdminFilter implements Filter
{
    private static final String LOGIN_SERVLET_PATH = "/plugins/servlet/applinks/login";

    private final AdminUIAuthenticator uiAuthenticator;

    public AdminFilter(final AdminUIAuthenticator uiAuthenticator)
    {
        this.uiAuthenticator = uiAuthenticator;
    }

    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException
    {
        if (!(servletRequest instanceof HttpServletRequest))
        {
            return;
        }

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        final HttpServletResponse response = (HttpServletResponse) servletResponse;

        if (!LOGIN_SERVLET_PATH.equals(request.getPathInfo()))
        {
            final String username = request.getParameter(AdminUIAuthenticator.ADMIN_USERNAME);
            final String password = request.getParameter(AdminUIAuthenticator.ADMIN_PASSWORD);

            if (!uiAuthenticator.canAccessAdminUI(username, password, new ServletSessionHandler(request)))
            {
                response.sendRedirect(
                        new StringBuilder(request.getContextPath())
                                .append(LOGIN_SERVLET_PATH)
                                .append("?").append(AdminLoginServlet.ORIGINAL_URL)
                                .append("=").append(getOriginalUrl(request)).toString()
                );
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getOriginalUrl(final HttpServletRequest request)
            throws UnsupportedEncodingException
    {
        final String originalUrl = new StringBuilder(request.getContextPath())
                .append(request.getServletPath())
                .append(request.getPathInfo())
                .append(sanitiseQueryString(request))
                .toString();
        return URLEncoder.encode(originalUrl, "UTF-8");
    }

    private String sanitiseQueryString(final HttpServletRequest request)
    {
        String queryString = request.getQueryString();

        if (queryString == null)
        {
            queryString = "";
        }
        else
        {
            queryString = queryString.replaceAll("(&|^)al_(username|password)=[^&]*", "");
            if (queryString.length() > 0)
            {
                queryString = "?" + queryString;
            }
        }

        return queryString;
    }

    public void init(final FilterConfig filterConfig) throws ServletException
    {
        // sun rise
    }

    public void destroy()
    {
        // sun set
    }
}
