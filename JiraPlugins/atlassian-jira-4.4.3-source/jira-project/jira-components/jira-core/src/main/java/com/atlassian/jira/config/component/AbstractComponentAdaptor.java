package com.atlassian.jira.config.component;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

public abstract class AbstractComponentAdaptor<T> implements ComponentAdapter
{
    protected PicoContainer container;
    protected final Class<T> interfaceClass;

    public AbstractComponentAdaptor(final Class<T> interfaceClass)
    {
        this.interfaceClass = interfaceClass;
    }

    public Object getComponentKey()
    {
        return interfaceClass;
    }

    public void verify() throws UnsatisfiableDependenciesException
    {}

    public PicoContainer getContainer()
    {
        return container;
    }

    public void setContainer(final PicoContainer picoContainer)
    {
        container = picoContainer;
    }

    public abstract Class<?> getComponentImplementation();

    public abstract Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException;
}
