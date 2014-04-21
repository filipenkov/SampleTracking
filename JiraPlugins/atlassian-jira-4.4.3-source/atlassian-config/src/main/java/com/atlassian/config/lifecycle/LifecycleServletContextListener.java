package com.atlassian.config.lifecycle;

import com.atlassian.spring.container.ContainerManager;
import com.atlassian.config.util.BootstrapUtils;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Servlet context listener to synchronise lifecycle startup and shutdown with the web application.
 */
public class LifecycleServletContextListener implements ServletContextListener
{
    public void contextInitialized(ServletContextEvent servletContextEvent)
    {
        if (shouldRunLifecycle())
            getLifecycleManager().startUp(servletContextEvent.getServletContext());
    }

    public void contextDestroyed(ServletContextEvent servletContextEvent)
    {
        if (shouldRunLifecycle())
            getLifecycleManager().shutDown(servletContextEvent.getServletContext());
    }

    private LifecycleManager getLifecycleManager()
    {
        return ((LifecycleManager) ContainerManager.getComponent("lifecycleManager"));
    }

    private boolean shouldRunLifecycle()
    {
        return ContainerManager.isContainerSetup() && BootstrapUtils.getBootstrapManager().isSetupComplete();
    }

}
