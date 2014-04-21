package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.Project;

/**
 * Simple cache that can be used to store objects for the current request. The objects will no longer be held
 * after the request has finished.
 *
 * @since v4.4
 */
public interface ProjectConfigRequestCache
{
    public Project getProject();
    public void setProject(Project project);

    public Object get(String key);
    public void put(String key, Object object);
}
