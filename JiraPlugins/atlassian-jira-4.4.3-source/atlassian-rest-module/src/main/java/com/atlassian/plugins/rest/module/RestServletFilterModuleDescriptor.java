package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugin.servlet.ServletModuleManager;
import com.atlassian.plugin.servlet.descriptors.ServletFilterModuleDescriptor;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.google.common.base.Preconditions;

import javax.servlet.Filter;

/**
 * The module descriptor for the REST servlet. Registered dynamically by the {@link RestModuleDescriptor}.
 * Uses the specific {@link RestDelegatingServletFilter}.
 */
public class RestServletFilterModuleDescriptor extends ServletFilterModuleDescriptor
{
    private final OsgiPlugin plugin;
    private final RestDelegatingServletFilter restDelegatingServletFilter;
    private final RestApiContext restApiContext;

    RestServletFilterModuleDescriptor(OsgiPlugin plugin, ModuleFactory moduleFactory, ServletModuleManager servletModuleManager, RestApiContext restApiContext)
    {
        super(Preconditions.checkNotNull(moduleFactory), Preconditions.checkNotNull(servletModuleManager));
        this.restApiContext = Preconditions.checkNotNull(restApiContext);
        this.plugin = Preconditions.checkNotNull(plugin);
        this.restDelegatingServletFilter = new RestDelegatingServletFilter(plugin, restApiContext);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
    }

    @Override
    public String getName()
    {
        return "Rest Servlet Filter";
    }

    protected void autowireObject(Object obj)
    {
        plugin.autowire(obj);
    }

    @Override
    public Filter getModule()
    {
        return restDelegatingServletFilter;
    }

    public String getBasePath()
    {
        return restApiContext.getApiPath();
    }

    public ApiVersion getVersion()
    {
        return restApiContext.getVersion();
    }
}
