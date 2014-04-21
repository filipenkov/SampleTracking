package com.atlassian.applinks.core.rest.model;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.rest.model.adapter.ApplicationLinkStateAdapter;
import com.atlassian.applinks.core.rest.util.EntityUtil;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Set;

/**
 * This entity is used for displaying purposes in the list application link screen.
 * This entity contains a {@link com.atlassian.applinks.core.rest.model.ApplicationLinkEntity}
 * plus information about which {@link com.atlassian.applinks.api.auth.AuthenticationProvider}s
 * are configured for outbound authentication.
 *
 * @since 3.0
 */
@XmlRootElement (name = "linkAndAuthProviderEntity")
public class LinkAndAuthProviderEntity
{
    private ApplicationLinkEntity application;
    private Set<String> configuredOutboundAuthenticators;
    private Set<String> configuredInboundAuthenticators;
    private boolean hasIncomingAuthenticationProviders;
    private boolean hasOutgoingAuthenticationProviders;
    private List<WebItemEntity> webItems;
    private List<WebPanelEntity> webPanels;
    @XmlJavaTypeAdapter(ApplicationLinkStateAdapter.class)
    private ApplicationLinkState appLinkState;

    /**
     * The set of entity types this application supports.
     */
    private Set<String> entityTypeIdStrings;

    public LinkAndAuthProviderEntity()
    {
    }

    public LinkAndAuthProviderEntity(final ApplicationLinkEntity applicationLinkEntity,
            final Set<Class<? extends AuthenticationProvider>> configuredOutboundAuthenticators,
            final Set<Class<? extends AuthenticationProvider>> configuredInboundAuthenticators,
            final boolean hasOutgoingAuthenticationProviders,
            final boolean hasIncomingAuthenticationProviders,
            final List<WebItemEntity> webItems,
            final List<WebPanelEntity> webPanels,
            final ApplicationLinkState appLinkState,
            final Set<String> entityTypeIdStrings)
    {
        this.hasOutgoingAuthenticationProviders = hasOutgoingAuthenticationProviders;
        this.hasIncomingAuthenticationProviders = hasIncomingAuthenticationProviders;
        this.webItems = webItems;
        this.webPanels = webPanels;
        this.application = applicationLinkEntity;
        this.configuredOutboundAuthenticators = EntityUtil.getClassNames(configuredOutboundAuthenticators);
        this.configuredInboundAuthenticators = EntityUtil.getClassNames(configuredInboundAuthenticators);
        this.appLinkState = appLinkState;
        this.entityTypeIdStrings = entityTypeIdStrings;
    }

    public Set<String> getConfiguredOutboundAuthenticators()
    {
        return configuredOutboundAuthenticators;
    }

    public ApplicationLinkEntity getApplication()
    {
        return application;
    }

    public boolean hasIncomingAuthenticationProviders()
    {
        return hasIncomingAuthenticationProviders;
    }

    public boolean hasOutgoingAuthenticationProviders()
    {
        return hasOutgoingAuthenticationProviders;
    }

    public List<WebItemEntity> getWebItems()
    {
        return webItems;
    }

    public List<WebPanelEntity> getWebPanels()
    {
        return webPanels;
    }

    public ApplicationLinkState getAppLinkState() {
        return appLinkState;
    }

    public Set<String> getEntityTypeIdStrings()
    {
        return entityTypeIdStrings;
    }

    public Set<String> getConfiguredInboundAuthenticators()
    {
        return configuredInboundAuthenticators;
    }
}
