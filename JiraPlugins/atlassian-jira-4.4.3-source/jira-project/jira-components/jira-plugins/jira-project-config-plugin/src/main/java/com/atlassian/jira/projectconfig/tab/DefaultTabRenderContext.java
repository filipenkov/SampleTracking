package com.atlassian.jira.projectconfig.tab;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * @since v4.4
 */
public class DefaultTabRenderContext implements ProjectConfigTabRenderContext
{
    private final Project project;
    private final String pathInfo;
    private final JiraAuthenticationContext authenticationContext;
    private final WebResourceManager webResourceManager;

    public DefaultTabRenderContext(String pathInfo, Project project, JiraAuthenticationContext ctx, WebResourceManager webResourceManager)
    {
        this.pathInfo = pathInfo;
        this.project = project;
        this.authenticationContext = ctx;
        this.webResourceManager = webResourceManager;
    }

    public Project getProject()
    {
        return project;
    }

    public HttpServletRequest getRequest()
    {
        return ExecutingHttpRequest.get();
    }

    public String getPathInfo()
    {
        return pathInfo;
    }

    public I18nHelper getI18NHelper()
    {
        return authenticationContext.getI18nHelper();
    }

    @Override
    public WebResourceManager getResourceManager()
    {
        return webResourceManager;
    }

    public Locale getLocale()
    {
        return authenticationContext.getLocale();
    }

    public User getUser()
    {
        return authenticationContext.getLoggedInUser();
    }
}
