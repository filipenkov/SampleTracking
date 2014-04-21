package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.hostcontainer.HostContainer;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.osgi.external.SingleModuleDescriptorFactory;
import com.atlassian.plugins.rest.module.servlet.RestServletModuleManager;
import com.google.common.base.Preconditions;

/**
 * Module descriptor factory for REST module descriptors.
 */
public class RestModuleDescriptorFactory extends SingleModuleDescriptorFactory<RestModuleDescriptor>
{
    private final RestServletModuleManager servletModuleManager;

    private final ModuleFactory moduleFactory;
    private final String restContextPath;

    public RestModuleDescriptorFactory(HostContainer hostContainer, ModuleFactory moduleFactory, RestServletModuleManager servletModuleManager, String restContextPath)
    {
        super(Preconditions.checkNotNull(hostContainer), "rest", RestModuleDescriptor.class);
        this.moduleFactory = moduleFactory;
        this.servletModuleManager = Preconditions.checkNotNull(servletModuleManager);
        this.restContextPath = Preconditions.checkNotNull(restContextPath);
    }

    @Override
    public ModuleDescriptor getModuleDescriptor(String type) throws PluginParseException, IllegalAccessException, InstantiationException, ClassNotFoundException
    {
        return hasModuleDescriptor(type) ? new RestModuleDescriptor(moduleFactory, servletModuleManager, restContextPath) : null;
    }
}
