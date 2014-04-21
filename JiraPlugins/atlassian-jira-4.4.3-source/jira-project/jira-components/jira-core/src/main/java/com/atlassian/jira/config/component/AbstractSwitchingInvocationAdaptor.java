package com.atlassian.jira.config.component;

import com.atlassian.util.concurrent.LazyReference;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public abstract class AbstractSwitchingInvocationAdaptor<T> extends AbstractComponentAdaptor<T>
{
    private final Class<T> enabledClass;
    private final Class<T> disabledClass;
    private final LazyReference<Object> componentReference = new LazyReference<Object>()
    {
        protected Object create() throws Exception
        {
            return Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{interfaceClass}, getHandler());
        }
    };

    public AbstractSwitchingInvocationAdaptor(Class<T> interfaceClass, Class<T> enabledClass, Class<T> disabledClass)
    {
        super(interfaceClass);
        this.enabledClass = enabledClass;
        this.disabledClass = disabledClass;
    }

    protected boolean isEnabled()
    {
        return getInvocationSwitcher().isEnabled();
    }

    public Class<T> getComponentImplementation()
    {
        return isEnabled() ? enabledClass : disabledClass;
    }

    protected InvocationHandler getHandler()
    {
        Object enabled = container.getComponentInstance(enabledClass);
        Object disabled = container.getComponentInstance(disabledClass);

        return new SwitchingInvocationHandler(enabled, disabled, getInvocationSwitcher());
    }

    public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
    {
        return componentReference.get();
    }

   protected abstract InvocationSwitcher getInvocationSwitcher();
}
