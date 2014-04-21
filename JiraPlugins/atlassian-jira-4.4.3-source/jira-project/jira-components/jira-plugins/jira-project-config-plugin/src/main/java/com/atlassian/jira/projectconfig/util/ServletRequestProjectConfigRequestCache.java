package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.web.ExecutingHttpRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Simple implementation of {@link ProjectConfigRequestCache} that uses the current ServletRequest to store all the
 * cache's data.
 *
 * @since v4.4
 */
public class ServletRequestProjectConfigRequestCache implements ProjectConfigRequestCache
{
    private static final String KEY_PREFIX = ServletRequestProjectConfigRequestCache.class.getName() + ":";
    private static final String KEY_PROJECT = "project";

    @Override
    public Project getProject()
    {
        Project project = (Project) get(KEY_PROJECT);
        if (project == null)
        {
            throw new IllegalStateException("The current project has not been set.");
        }
        return project;
    }

    @Override
    public void setProject(Project project)
    {
        put(KEY_PROJECT, project);
    }

    @Override
    public Object get(String key)
    {
        return getRequest().getAttribute(createKey(key));
    }

    @Override
    public void put(String key, Object object)
    {
        getRequest().setAttribute(createKey(key), object);
    }

    private static String createKey(String key)
    {
        return KEY_PREFIX + key;
    }

    HttpServletRequest getRequest()
    {
        HttpServletRequest request = ExecutingHttpRequest.get();
        if (request == null)
        {
            throw new IllegalStateException("No current web request is running.");
        }
        return request;
    }
}
