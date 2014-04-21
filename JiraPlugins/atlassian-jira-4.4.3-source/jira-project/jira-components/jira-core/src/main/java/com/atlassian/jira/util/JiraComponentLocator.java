package com.atlassian.jira.util;

import com.atlassian.jira.ComponentManager;

/**
 * Implementation of the {@link com.atlassian.jira.util.ComponentLocator} interface. Uses JIRAs {@link
 * com.atlassian.jira.ComponentManager#getComponentInstanceOfType(Class)} method to actually do the lookup.
 *
 * @since v4.0
 */
public class JiraComponentLocator implements ComponentLocator
{
    public <T> T getComponentInstanceOfType(final Class<T> type)
    {
        return ComponentManager.getComponentInstanceOfType(type);
    }

    public <T> T getComponent(final Class<T> type)
    {
        return getComponentInstanceOfType(type);
    }

    @Override
    public String toString()
    {
        return "Default Component locator";
    }
}
