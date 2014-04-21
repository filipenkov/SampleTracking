package com.atlassian.config.lifecycle;

import javax.servlet.ServletContextListener;
import javax.servlet.ServletContextEvent;

/**
 * Wrapper to convert a ServletContextListener into a LifecycleItem. This makes it easy to move code designed to run
 * from web.xml into the lifecycle system.
 */
public class ServletContextListenerWrapper implements LifecycleItem
{
    private final ServletContextListener listener;

    public ServletContextListenerWrapper(ServletContextListener listener)
    {
        this.listener = listener;
    }

    public void startup(LifecycleContext context)
    {
        listener.contextInitialized(new ServletContextEvent(context.getServletContext()));
    }

    public void shutdown(LifecycleContext context)
    {
        listener.contextDestroyed(new ServletContextEvent(context.getServletContext()));
    }

    public ServletContextListener getWrappedListener()
    {
        return listener;
    }
}
