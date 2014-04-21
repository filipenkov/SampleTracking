/*
 * Created on Nov 17, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package com.atlassian.spring.container;

import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;

/**
 * @author Ross Mason
 *         <p/>
 *         The confluence context listener will obtain a reference to the spring application context to use
 *         for resolving components.  This listener extends the Spring ContextLoaderListener and should be used instead of
 *         in the web.xml.
 *         <p/>
 *         The loader now fires an event once the ContainerManager has been setup with a context.
 * @see com.atlassian.spring.container.ContainerContextLoadedEvent
 */
public class ContainerContextLoaderListener extends ContextLoaderListener
{
    /* (non-Javadoc)
     * @see javax.servlet.ServletContextListener#contextInitialized(javax.servlet.ServletContextEvent)
     */
    public void contextInitialized(ServletContextEvent event)
    {
        if (canInitialiseContainer())
        {
            super.contextInitialized(event);
            postInitialiseContext(event);
        }

        SpringContainerContext springCC = getNewSpringContainerContext();
        springCC.setServletContext(event.getServletContext());
        ContainerManager.getInstance().setContainerContext(springCC);

        springCC.contextReloaded();
    }

    protected void postInitialiseContext(ServletContextEvent event)
    {
        
    }

    protected SpringContainerContext getNewSpringContainerContext()
    {
        return new SpringContainerContext();
    }

    /**
     * Overriding classes can apply contidional behaviour here
     *
     * @return
     */
    public boolean canInitialiseContainer()
    {
        return true;
    }

}
