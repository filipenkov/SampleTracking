package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Collections;
import java.util.List;
import static com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor.BY_WEIGHT;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since   v3.0
 */
public class AuthenticatorAccessor
{
    private final PluginAccessor pluginAccessor;

    public AuthenticatorAccessor(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    /**
     * @return  an {@link com.atlassian.applinks.api.auth.AuthenticationProvider}
     * instance that implements the specified type and is configured for use
     * with the specified {@link com.atlassian.applinks.api.ApplicationLink}.
     * When more than one {@link com.atlassian.applinks.api.auth.AuthenticationProvider}
     * satisfies these criteria, it is unspecified which one is returned.
     */
    @SuppressWarnings("unchecked")
    public <T extends AuthenticationProvider> T getAuthenticationProvider(final ApplicationLink applicationLink, final Class<T> providerClass)
    {
        checkNotNull(applicationLink);
        checkNotNull(providerClass);

        for (final AuthenticationProviderPluginModule module : getAllAuthenticationProviderPluginModules())
        {
            final AuthenticationProvider provider = module.getAuthenticationProvider(applicationLink);
            if (provider != null && providerClass.isAssignableFrom(provider.getClass()))
            {
                return (T) provider; // the cast is fine
            }
        }
        return null;
    }
    
    public Iterable<AuthenticationProviderPluginModule> getAllAuthenticationProviderPluginModules()
    {
    	final List<AuthenticationProviderModuleDescriptor> descriptors = Lists.newArrayList(pluginAccessor.getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class));
        Collections.sort(descriptors, BY_WEIGHT);

        return Iterables.transform(descriptors, new Function<AuthenticationProviderModuleDescriptor, AuthenticationProviderPluginModule>()
        {
            public AuthenticationProviderPluginModule apply(final AuthenticationProviderModuleDescriptor from)
            {
                return from.getModule();
            }
        });
    }
}
