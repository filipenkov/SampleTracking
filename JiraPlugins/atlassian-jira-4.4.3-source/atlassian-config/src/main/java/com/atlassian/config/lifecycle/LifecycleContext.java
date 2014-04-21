package com.atlassian.config.lifecycle;

import com.atlassian.johnson.JohnsonEventContainer;

import javax.servlet.ServletContext;

public interface LifecycleContext
{
    ServletContext getServletContext();
    JohnsonEventContainer getAgentJohnson();
}
