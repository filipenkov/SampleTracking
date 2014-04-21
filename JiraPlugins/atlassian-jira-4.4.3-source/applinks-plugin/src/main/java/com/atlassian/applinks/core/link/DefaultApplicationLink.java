package com.atlassian.applinks.core.link;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.api.event.ApplicationLinkDetailsChangedEvent;
import com.atlassian.applinks.core.auth.ApplicationLinkRequestFactoryFactory;
import com.atlassian.applinks.core.property.ApplicationLinkProperties;
import com.atlassian.applinks.spi.application.StaticUrlApplicationType;
import com.atlassian.applinks.spi.link.ApplicationLinkDetails;
import com.atlassian.event.api.EventPublisher;

import java.net.URI;

public class DefaultApplicationLink implements InternalApplicationLink
{
    private final ApplicationId id;
    private final ApplicationType type;
    private final ApplicationLinkProperties applicationLinkProperties;

    private final ApplicationLinkRequestFactoryFactory requestFactoryFactory;
    private final EventPublisher eventPublisher;

    public DefaultApplicationLink(
            final ApplicationId serverId,
            final ApplicationType type,
            final ApplicationLinkProperties applicationLinkProperties,
            final ApplicationLinkRequestFactoryFactory requestFactoryFactory,
            final EventPublisher eventPublisher)
    {
        this.id = serverId;
        this.applicationLinkProperties = applicationLinkProperties;
        this.requestFactoryFactory = requestFactoryFactory;
        this.type = type;
        this.eventPublisher = eventPublisher;
    }

    public void update(final ApplicationLinkDetails details)
    {
        applicationLinkProperties.setName(details.getName());
        applicationLinkProperties.setDisplayUrl(details.getDisplayUrl());
        //TODO: Do we support to change the RPC URL at all?
        applicationLinkProperties.setRpcUrl(details.getRpcUrl());
        
        eventPublisher.publish(new ApplicationLinkDetailsChangedEvent(this));
    }

    public void setPrimaryFlag(final boolean isPrimary)
    {
        applicationLinkProperties.setIsPrimary(isPrimary);
    }

    public ApplicationId getId()
    {
        return id;
    }

    public ApplicationType getType()
    {
        return type;
    }

    public String getName()
    {
        return applicationLinkProperties.getName();
    }

    public URI getDisplayUrl()
    {
        if (type instanceof StaticUrlApplicationType)
        {
            return ((StaticUrlApplicationType) type).getStaticUrl();
        }
        else
        {
            return applicationLinkProperties.getDisplayUrl();
        }
    }

    public URI getRpcUrl()
    {
        if (type instanceof StaticUrlApplicationType)
        {
            return ((StaticUrlApplicationType) type).getStaticUrl();
        }
        else
        {
            return applicationLinkProperties.getRpcUrl();
        }
    }

    public boolean isPrimary()
    {
        return applicationLinkProperties.isPrimary();
    }

    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory()
    {
        return requestFactoryFactory.getApplicationLinkRequestFactory(this);
    }

    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory(final Class<? extends AuthenticationProvider> providerClass)
    {
        return requestFactoryFactory.getApplicationLinkRequestFactory(this, providerClass);
    }

    public Object getProperty(final String key)
    {
        return applicationLinkProperties.getProperty(key);
    }

    public Object putProperty(final String key, final Object value)
    {
        return applicationLinkProperties.putProperty(key, value);
    }

    public Object removeProperty(final String key)
    {
        return applicationLinkProperties.removeProperty(key);
    }

    @Override
    public String toString()
    {
        return String.format("%s (%s) %s %s", getName(), id, getRpcUrl(), getType());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final DefaultApplicationLink that = (DefaultApplicationLink) o;

        if (id != null ? !id.equals(that.id) : that.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }
}
