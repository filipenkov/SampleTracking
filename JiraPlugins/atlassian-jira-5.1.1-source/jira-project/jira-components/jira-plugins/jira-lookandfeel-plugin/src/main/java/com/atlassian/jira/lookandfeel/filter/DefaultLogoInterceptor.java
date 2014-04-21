package com.atlassian.jira.lookandfeel.filter;


import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.lookandfeel.LookAndFeelConstants;
import com.atlassian.jira.lookandfeel.LookAndFeelProperties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * The entire purpose of this filter is to redirect all Logo requests to the default studio logo uploaded by sysadmins.
 */
public class DefaultLogoInterceptor implements Filter
{
    private FilterConfig config;
    private final JiraHome jiraHome;
    private final LookAndFeelProperties lookAndFeelProperties;
    private final String JIRA_LOGO="logo.png";

    public DefaultLogoInterceptor(final JiraHome jiraHome, final LookAndFeelProperties lookAndFeelProperties)
    {
        this.jiraHome = jiraHome;
        this.lookAndFeelProperties = lookAndFeelProperties;
    }


    public void init(final FilterConfig filterConfig) throws ServletException
    {
        this.config = filterConfig;
    }

    public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
            throws IOException, ServletException
    {
        if(request instanceof HttpServletRequest && lookAndFeelProperties.isUsingCustomDefaultLogo()) {
            final HttpServletResponse res = (HttpServletResponse )response;
            final HttpServletRequest req = (HttpServletRequest)request;
            String requestURL = req.getRequestURL().toString();
            if(requestURL.endsWith(JIRA_LOGO)) {
                ImageDownloader downloader = new ImageDownloader();
                downloader.doDownload(req, res, config.getServletContext(), jiraHome.getHomePath()+"/logos/"+ LookAndFeelConstants.JIRA_SCALED_DEFAULT_LOGO_FILENAME, true);
            }
        }
        else
        {
            chain.doFilter(request, response);
        }
    }

    public void destroy() { }
}
