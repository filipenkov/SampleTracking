/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: May 28, 2004
 * Time: 2:39:49 PM
 */
package com.atlassian.spring.container;

import org.springframework.context.ApplicationEvent;

/**
 * An event published when the ContainerContext has been fully loaded and the ContainerManager is ready for us.
 *
 * @see com.atlassian.spring.container.ContainerContext
 * @see com.atlassian.spring.container.ContainerContextLoaderListener
 */
public class ContainerContextLoadedEvent extends ApplicationEvent
{
    public ContainerContextLoadedEvent(Object o)
    {
        super(o);
    }
}