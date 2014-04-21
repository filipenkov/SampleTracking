package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Inherit from this base class when you implement {@link com.atlassian.applinks.host.spi.InternalHostApplication}.
 *  This base class provides implementations for getSupportedOutboundAuthenticationTypes and getSupportedInboundAuthenticationTypes.
 *  It queries the plugin system for enabled {@link com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule} to detect supported outbound
 *  authentication types and for enabled {@link com.atlassian.applinks.host.spi.SupportedInboundAuthenticationModuleDescriptor} to detect
 *  inbound authentication types.
 *
 * @since 3.0
 */
public abstract class AbstractInternalHostApplication implements InternalHostApplication
{
    protected final PluginAccessor pluginAccessor;

    protected AbstractInternalHostApplication(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public Iterable<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
        return Iterables.transform(pluginAccessor.getEnabledModuleDescriptorsByClass(SupportedInboundAuthenticationModuleDescriptor.class),
                new Function<SupportedInboundAuthenticationModuleDescriptor, Class<? extends AuthenticationProvider>>()
                {
                    public Class<? extends AuthenticationProvider> apply(final SupportedInboundAuthenticationModuleDescriptor from)
                    {
                        return from.getAuthenticationProviderClass();
                    }
                });
    }


    @SuppressWarnings("unchecked")
    public Iterable<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        return Iterables.transform(pluginAccessor.getEnabledModulesByClass(AuthenticationProviderPluginModule.class),
                new Function<AuthenticationProviderPluginModule, Class<? extends AuthenticationProvider>>()
                {
                    public Class<? extends AuthenticationProvider> apply(final AuthenticationProviderPluginModule from)
                    {
                        return from.getAuthenticationProviderClass();
                    }
                });
    }
}