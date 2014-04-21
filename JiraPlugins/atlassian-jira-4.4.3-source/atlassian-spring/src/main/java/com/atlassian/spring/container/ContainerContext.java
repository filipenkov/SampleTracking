/*
 * Copyright (c) 2003 by Atlassian Software Systems Pty. Ltd. All rights reserved.
 */
package com.atlassian.spring.container;

import com.atlassian.event.Event;
import org.springframework.beans.factory.config.BeanPostProcessor;

public interface ContainerContext
{
    /**
     * Retrieves a component from the container
     * @param key the key which matches to the component
     * @return the component or null if the component was not found
     * @throws com.atlassian.spring.container.ComponentNotFoundException if the key passed in is null or the component is not found
     * or if there is more than one satisfiable component for the given key, such as the key is a Class
     * and multiple instances of the class exist in the container
     */
    Object getComponent(Object key) throws ComponentNotFoundException;

    /**
     * Create an object of the given class, and try to auto-wire by name.
     */
    Object createComponent(Class clazz);

    /**
     * Creates a new bean from the given class. Also executes any lifecyle process like {@link BeanPostProcessor}s and autowires
     * the bean.
     *
     * @param clazz @NotNull
     * @return A fully constructed object of the passed class. May be a proxy of the class.
     */
    Object createCompleteComponent(Class clazz);

    /**
     * Autowire an object in this container as much as we can.
     */
    void autowireComponent(Object component);

    /**
     * Refreshes the container, i.e. reloads it's components from it's config file
     */
    void refresh();

    boolean isSetup();

    void publishEvent(Event e);
}
