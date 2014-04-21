package com.atlassian.plugins.rest.module.servlet;

import com.atlassian.plugin.servlet.ServletModuleContainerServlet;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.filter.ServletFilterModuleContainerFilter;
import com.google.common.base.Preconditions;

/**
 * A {@link ServletModuleContainerServlet} for REST dispatching. Typically a new REST filter is created for each REST module
 * and this filter handles the main dispatching
 */
public class RestServletFilterModuleContainerServlet extends ServletFilterModuleContainerFilter
{
    private final ServletModuleManager servletModuleManager;

    public RestServletFilterModuleContainerServlet(RestServletModuleManager servletModuleManager)
    {
        this.servletModuleManager = Preconditions.checkNotNull(servletModuleManager);
    }

    protected ServletModuleManager getServletModuleManager()
    {
        return servletModuleManager;
    }
}
