package com.atlassian.applinks.core;

import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.module.ModuleFactory;

public class MockAuthenticationProviderModuleDescriptor extends AuthenticationProviderModuleDescriptor
{
    private final AuthenticationProviderPluginModule module;

    public MockAuthenticationProviderModuleDescriptor(final ModuleFactory moduleFactory, final AuthenticationProviderPluginModule module)
    {
        super(moduleFactory);
        this.module = module;
    }

    public AuthenticationProviderPluginModule getModule()
    {
        return module;
    }
}
