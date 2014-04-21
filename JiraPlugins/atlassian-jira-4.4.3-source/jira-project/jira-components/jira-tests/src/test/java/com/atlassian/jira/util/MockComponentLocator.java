package com.atlassian.jira.util;

/**
 * @since v4.3
 */
public class MockComponentLocator implements ComponentLocator
{
    public <T> T getComponentInstanceOfType(final Class<T> type)
    {
        return null;
    }
    
    public <T> T getComponent(final Class<T> type)
    {
        return getComponentInstanceOfType(type);
    }
}
