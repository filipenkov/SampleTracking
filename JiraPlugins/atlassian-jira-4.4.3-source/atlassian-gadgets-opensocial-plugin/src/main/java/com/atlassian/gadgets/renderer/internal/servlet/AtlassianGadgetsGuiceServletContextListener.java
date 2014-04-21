package com.atlassian.gadgets.renderer.internal.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.google.inject.Injector;
import com.google.inject.Provider;

import org.apache.shindig.common.servlet.GuiceServletContextListener;

public class AtlassianGadgetsGuiceServletContextListener implements ServletContextListener
{
    private final Injector injector;

    public AtlassianGadgetsGuiceServletContextListener(Provider<Injector> provider)
    {
        injector = provider.get();
    }
    
    public void contextInitialized(ServletContextEvent event)
    {
        ServletContext context = event.getServletContext();
        context.setAttribute(GuiceServletContextListener.INJECTOR_ATTRIBUTE, injector);
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        ServletContext context = event.getServletContext();
        context.removeAttribute(GuiceServletContextListener.INJECTOR_ATTRIBUTE);
    }
}
