package com.atlassian.jira.mock.pico;

import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoRegistrationException;
import org.picocontainer.PicoVerificationException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Mock PICO.
 *
 * @since v4.3
 */
public class MockPicoContainer implements MutablePicoContainer
{
    private final ConcurrentMap<Class<?>,Object> components = new ConcurrentHashMap<Class<?>,Object>();

    public <T> MockPicoContainer addComponent(Class<T> componentClass, T compInstance)
    {
        components.put(componentClass, compInstance);
        return this;
    }

    @Override
    public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation)
            throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter registerComponentImplementation(Object componentKey, Class componentImplementation, Parameter[] parameters)
            throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter registerComponentImplementation(Class componentImplementation)
            throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter registerComponentInstance(Object componentInstance) throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter registerComponentInstance(Object componentKey, Object componentInstance)
            throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter registerComponent(ComponentAdapter componentAdapter) throws PicoRegistrationException
    {
        return null;
    }

    @Override
    public ComponentAdapter unregisterComponent(Object componentKey)
    {
        return null;
    }

    @Override
    public ComponentAdapter unregisterComponentByInstance(Object componentInstance)
    {
        return null;
    }

    @Override
    public void setParent(PicoContainer parent)
    {
    }

    @Override
    public Object getComponentInstance(Object componentKey)
    {
        return components.get(componentKey);
    }

    @Override
    public Object getComponentInstanceOfType(Class componentType)
    {
        return components.get(componentType);
    }

    @Override
    public List getComponentInstances()
    {
        return new ArrayList<Object>(components.values());
    }

    @Override
    public PicoContainer getParent()
    {
        return null;
    }

    @Override
    public ComponentAdapter getComponentAdapter(Object componentKey)
    {
        return null;
    }

    @Override
    public ComponentAdapter getComponentAdapterOfType(Class componentType)
    {
        return null;
    }

    @Override
    public Collection getComponentAdapters()
    {
        return null;
    }

    @Override
    public List getComponentAdaptersOfType(Class componentType)
    {
        return null;
    }

    @Override
    public void verify() throws PicoVerificationException
    {
    }

    @Override
    public void addOrderedComponentAdapter(ComponentAdapter componentAdapter)
    {
    }

    @Override
    public void dispose()
    {
    }

    @Override
    public void start()
    {
    }

    @Override
    public void stop()
    {
    }
}
