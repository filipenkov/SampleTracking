package com.atlassian.applinks.core.auth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.event.ApplicationLinkAuthConfigChangedEvent;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.core.property.PropertyService;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.event.api.EventPublisher;
import com.google.common.base.Preconditions;

import java.util.Map;

/**
 * @since 3.0
 */
public class AuthenticationConfigurationManagerImpl implements AuthenticationConfigurationManager
{
    private final ApplicationLinkService applicationLinkService;
    private final PropertyService propertyService;
    private final EventPublisher eventPublisher;

    public AuthenticationConfigurationManagerImpl(final ApplicationLinkService applicationLinkService, final PropertyService propertyService, final EventPublisher eventPublisher)
    {
        this.applicationLinkService = applicationLinkService;
        this.propertyService = propertyService;
        this.eventPublisher = eventPublisher;
    }

    public Map<String, String> getConfiguration(final ApplicationId id, final Class<? extends AuthenticationProvider> provider)
    {
        assertApplicationLinkPresence(id);
        return propertyService.getApplicationLinkProperties(id).getProviderConfig(getPrefixForProvider(provider));
    }

    public boolean isConfigured(final ApplicationId id, final Class<? extends AuthenticationProvider> provider)
    {
        return propertyService.getApplicationLinkProperties(id).authProviderIsConfigured(getPrefixForProvider(provider));
    }

    public void registerProvider(final ApplicationId id, final Class<? extends AuthenticationProvider> provider, final Map<String, String> config)
    {
        assertApplicationLinkPresence(id);
        final ApplicationLinkProperties props = propertyService.getApplicationLinkProperties(id);
        props.setProviderConfig(getPrefixForProvider(provider), config);
        publishChangeEvent(id);
    }

    public void unregisterProvider(final ApplicationId id, final Class<? extends AuthenticationProvider> provider)
    {
        assertApplicationLinkPresence(id);
        final ApplicationLinkProperties props = propertyService.getApplicationLinkProperties(id);
        props.removeProviderConfig(getPrefixForProvider(provider));
        publishChangeEvent(id);
    }

    private void assertApplicationLinkPresence(final ApplicationId id)
    {
        final ApplicationLink applicationLink;
        applicationLink = getApplicationLink(id);

        if (applicationLink == null)
        {
            throw new IllegalArgumentException(String.format(
                    "Application Link \"%s\" not found.", id));
        }
    }
    
    private ApplicationLink getApplicationLink(final ApplicationId id)
    {
        final ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(id);
        }
        catch (TypeNotInstalledException e)
        {
            throw new IllegalStateException(String.format(
                    "Failed to load application link %s as type %s is no longer installed.",
                    id, e.getType()));
        }
        return applicationLink;
    }

    private String getPrefixForProvider(final Class<? extends AuthenticationProvider> provider)
    {
        return Preconditions.checkNotNull(provider, "AuthenticationProvider").getName();
    }
    
    private void publishChangeEvent(final ApplicationId id)
    {
        final ApplicationLink link = getApplicationLink(id);
        if (link != null)
        {
            eventPublisher.publish(new ApplicationLinkAuthConfigChangedEvent(link));
        }
    }
}
