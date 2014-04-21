package com.atlassian.jira.mock.component;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.fields.FieldAccessor;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactory;
import com.atlassian.jira.jql.builder.JqlClauseBuilderFactoryImpl;
import com.atlassian.jira.jql.util.JqlDateSupportImpl;

import java.util.HashMap;
import java.util.Map;

/**
 * This component worker can be used with the component accessor, return mock instances of
 * components for unit testing.
 *
 * @since v4.4
 */
public class MockComponentWorker implements ComponentAccessor.Worker
{
    Map<Class, Object> implementations = new HashMap<Class, Object>();

    public MockComponentWorker()
    {
        // Add some default mocks
        registerMock(JqlClauseBuilderFactory.class, new JqlClauseBuilderFactoryImpl(new JqlDateSupportImpl(null)));
    }

    public <T, U extends T> void registerMock(Class<T> componentInterface, U componentMock)
    {
        implementations.put(componentInterface, componentMock);
    }

    public <T, U extends T> MockComponentWorker addMock(Class<T> componentInterface, U componentMock)
    {
        registerMock(componentInterface, componentMock);
        return this;
    }

    @SuppressWarnings ( { "unchecked" })
    @Override
    public <T> T getComponent(Class<T> componentClass)
    {
        return (T) implementations.get(componentClass);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getComponentOfType(Class<T> componentClass)
    {
        return getComponent(componentClass);
    }

    @Override
    public <T> T getOSGiComponentInstanceOfType(Class<T> componentClass)
    {
        return getComponent(componentClass);
    }

    @Override
    public FieldAccessor getFieldAccessor()
    {
        return null;
    }
}
