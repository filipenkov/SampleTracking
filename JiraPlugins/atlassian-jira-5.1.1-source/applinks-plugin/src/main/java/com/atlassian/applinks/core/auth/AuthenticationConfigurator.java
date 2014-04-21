package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationException;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.applinks.spi.auth.AuthenticationScenario;
import com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.net.RequestFactory;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

/**
 * This component delegates the configuration of authentication after an application link has been created to an authentication
 * provider that implements {@link com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule}.
 *
 * @since 3.0
 */
public class AuthenticationConfigurator
{
    private final PluginAccessor pluginAccessor;
    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationConfigurator.class);

    public AuthenticationConfigurator(final PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    /**
     * This method will iterate over all authentication provider that implement {@link
     * com.atlassian.applinks.spi.auth.AutoConfiguringAuthenticatorProviderPluginModule} ordered by their weight
     * and it will use the first one that accepts the scenario {@link com.atlassian.applinks.spi.auth.AuthenticationScenario} to configure authentication
     * for the local and remote application link.
     *
     * @param applicationLink the new application link
     * @param scenario the input from the form
     * @param requestFactory a request factory that will make authenticated requests to the remote application.
     *
     * @return true if one authentication provider could be configured, otherwise false if no auto-configuration happened.
     *
     * @throws AuthenticationConfigurationException if no authentication provider could be auto-configured and one or more authentication provider
     *         threw a {@link com.atlassian.applinks.spi.auth.AuthenticationConfigurationException}.
     */
    public boolean configureAuthenticationForApplicationLink(final ApplicationLink applicationLink,
                                                             final AuthenticationScenario scenario,
                                                             final RequestFactory requestFactory)
            throws AuthenticationConfigurationException

    {
        final List<AuthenticationProviderModuleDescriptor> descriptors = Lists.newArrayList(pluginAccessor
                .getEnabledModuleDescriptorsByClass(AuthenticationProviderModuleDescriptor.class));
        Collections.sort(descriptors, AuthenticationProviderModuleDescriptor.BY_WEIGHT);
        AuthenticationConfigurationException exception = null;
        for (AuthenticationProviderModuleDescriptor descriptor : descriptors)
        {
            final AuthenticationProviderPluginModule module = descriptor.getModule();
            if (module instanceof AutoConfiguringAuthenticatorProviderPluginModule)
            {
                final AutoConfiguringAuthenticatorProviderPluginModule configurableModule =
                        (AutoConfiguringAuthenticatorProviderPluginModule) module;
                if (configurableModule.isApplicable(scenario, applicationLink))
                {
                    try
                    {
                        configurableModule.enable(requestFactory, applicationLink);
                        LOG.debug("Configured authentication provider '{}' for application link '{}'",
                                configurableModule.getClass().getName(), applicationLink.getId().toString());
                        return true;
                    }
                    catch (AuthenticationConfigurationException e)
                    {
                        LOG.warn("Failed to initialize authentication provider '" +
                                configurableModule.getAuthenticationProviderClass().getName() +
                                "'. Trying to use another one.", e);
                        exception = e;
                    }
                }
            }
        }
        LOG.debug("No authentication provider auto-configured " +
                "for the new application link '{}'.", applicationLink.getId().toString());
        if (exception != null)
        {
            throw new AuthenticationConfigurationException("No authentication " +
                    "provider configured and one or more authentication " +
                    "provider threw an exception during auto-configuration.",
                    exception);
        }
        return false;
    }

}
