package com.atlassian.jira.projectconfig.tab;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugin.webresource.WebResourceManager;

import javax.servlet.http.HttpServletRequest;
import java.util.Locale;

/**
 * Contains useful dependenics that can be used when renderering a {@link ProjectConfigTab}.
 *
 * @since v4.4
 */
public interface ProjectConfigTabRenderContext
{
    public Project getProject();
    public HttpServletRequest getRequest();
    public String getPathInfo();
    public Locale getLocale();
    public User getUser();
    public I18nHelper getI18NHelper();
    public WebResourceManager getResourceManager();
}
