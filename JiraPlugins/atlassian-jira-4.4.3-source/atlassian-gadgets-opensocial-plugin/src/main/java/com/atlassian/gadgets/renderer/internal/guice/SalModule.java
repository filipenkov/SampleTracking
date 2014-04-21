package com.atlassian.gadgets.renderer.internal.guice;

import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.pluginsettings.PluginSettingsFactory;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.security.auth.trustedapps.TrustedApplicationsManager;

import com.google.inject.AbstractModule;

public class SalModule extends AbstractModule
{
    private final ApplicationProperties applicationProperties;
    private final TrustedApplicationsManager trustedApplicationsManager;
    private final UserManager userManager;
    private final PluginSettingsFactory pluginSettingsFactory;

    public SalModule(ApplicationProperties applicationProperties,
        TrustedApplicationsManager trustedApplicationsManager,
        UserManager userManager,
        PluginSettingsFactory pluginSettingsFactory)
    {
        this.applicationProperties = applicationProperties;
        this.trustedApplicationsManager = trustedApplicationsManager;
        this.userManager = userManager;
        this.pluginSettingsFactory = pluginSettingsFactory;
    }

    @Override
    protected void configure()
    {
        bind(ApplicationProperties.class).toInstance(applicationProperties);
        bind(TrustedApplicationsManager.class).toInstance(trustedApplicationsManager);
        bind(UserManager.class).toInstance(userManager);
        bind(PluginSettingsFactory.class).toInstance(pluginSettingsFactory);
    }
}
