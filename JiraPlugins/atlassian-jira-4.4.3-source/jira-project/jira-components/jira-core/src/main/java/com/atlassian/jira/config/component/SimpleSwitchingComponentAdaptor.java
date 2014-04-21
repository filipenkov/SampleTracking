/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.config.component;

import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;

/**
 * A simple component adapter that will pull the implementation of the object from the container.
 * <p>
 * Sub classes will need to implement {@link #getComponentImplementation()} and specify the correct class.
 * <p>
 * This class returns the object directly (no fancy dynamic proxies), so consumers of these objects
 * should not cache them if they wish to be updated when the object changes.
 */
public abstract class SimpleSwitchingComponentAdaptor extends AbstractComponentAdaptor
{
    public SimpleSwitchingComponentAdaptor(Class interfaceClass)
    {
        super(interfaceClass);
    }

    public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
    {
        return container.getComponentInstanceOfType(getComponentImplementation());
    }

    public abstract Class getComponentImplementation();

}
