package com.atlassian.jira.component;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.fields.FieldAccessor;
import com.atlassian.jira.issue.fields.FieldManager;

/**
 * Worker class that insulates the API from the implementation dependencies in ManagerFactory etc.
 *
 * @since v4.3
 */
public class ComponentAccessorWorker implements ComponentAccessor.Worker
{
    @Override
    public <T> T getComponent(Class<T> componentClass)
    {
        return ComponentManager.getComponent(componentClass);
    }

    @Override
    public <T> T getComponentOfType(Class<T> componentClass)
    {
        return ComponentManager.getComponentInstanceOfType(componentClass);
    }

    @Override
    public <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return ComponentManager.getOSGiComponentInstanceOfType(componentClass);
    }

    @Override
    public FieldAccessor getFieldAccessor()
    {
        // return FieldManager instance.
        return getComponentOfType(FieldManager.class);
    }
}