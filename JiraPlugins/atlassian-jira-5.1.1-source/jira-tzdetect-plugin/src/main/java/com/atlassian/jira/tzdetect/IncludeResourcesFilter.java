package com.atlassian.jira.tzdetect;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.CoreFeatures;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 */
public class IncludeResourcesFilter implements Filter
{
    private final WebResourceManager webResourceManager;
    private final BannerPreferences bannerPreferences;

    public IncludeResourcesFilter(WebResourceManager webResourceManager, BannerPreferences bannerPreferences)
    {
        this.webResourceManager = webResourceManager;
        this.bannerPreferences = bannerPreferences;
    }

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain)
            throws IOException, ServletException
    {
        if (bannerPreferences.isDisplayBanner() && isRelevantPath(servletRequest))
        {
            webResourceManager.requireResource("com.atlassian.jira.jira-tzdetect-plugin:tzdetect-lib");
        }
        
        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isRelevantPath(ServletRequest servletRequest)
    {
        if (featureManager().isEnabled(CoreFeatures.ON_DEMAND))
        {
            // restrict to known JIRA-specific paths
            final String requestPath = getServletPath(servletRequest);
            return requestPath.contains("/browse") // issues & projects
                    || requestPath.endsWith("/secure/IssueNavigator.jspa")
                    || requestPath.endsWith("/secure/Dashboard.jspa")
                    || requestPath.endsWith("/secure/RapidBoard.jspa");
        }
        else
        {
            return true;
        }
    }

    private String getServletPath(ServletRequest servletRequest)
    {
        if (!HttpServletRequest.class.isInstance(servletRequest))
        {
            throw new IllegalStateException("This is not HTTP request: " + servletRequest + ". I'm confused");
        }
        return ((HttpServletRequest) servletRequest).getServletPath();
    }

    private FeatureManager featureManager()
    {
        return ComponentAccessor.getComponent(FeatureManager.class);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException
    {
    }

    @Override
    public void destroy()
    {
    }
}
