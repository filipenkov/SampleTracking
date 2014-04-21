package com.atlassian.jira.projectconfig.util;

import com.atlassian.jira.project.Project;
import com.google.common.collect.Maps;

import java.util.Map;

/**
 * @since v4.4
 */
public class MockProjectConfigRequestCache implements ProjectConfigRequestCache
{
    private Project project;
    private Map<String, Object> cache = Maps.newHashMap();

    @Override
    public Project getProject()
    {
        return project;
    }

    @Override
    public void setProject(Project project)
    {
        this.project = project;
    }

    @Override
    public Object get(String key)
    {
        return cache.get(key);
    }

    @Override
    public void put(String key, Object object)
    {
        cache.put(key, object);
    }

    public Map<String, Object> getCache()
    {
        return cache;
    }
}
