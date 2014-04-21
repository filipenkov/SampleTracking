package com.atlassian.config.lifecycle;

import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.ServletContext;

public class DefaultLifecycleContext implements LifecycleContext
{
    private final ServletContext servletContext;
    private final JohnsonEventContainer johnsonEventContainer;

    public DefaultLifecycleContext(ServletContext servletContext)
    {
        this.servletContext = servletContext;
        this.johnsonEventContainer = JohnsonEventContainer.get(servletContext);
    }

    public ServletContext getServletContext()
    {
        return servletContext;
    }

    public JohnsonEventContainer getAgentJohnson()
    {
        return johnsonEventContainer;
    }
}
