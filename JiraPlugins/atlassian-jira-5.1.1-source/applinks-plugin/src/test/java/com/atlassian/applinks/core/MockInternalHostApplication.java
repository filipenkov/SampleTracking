package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.host.spi.InternalHostApplication;

import java.net.URI;
import java.util.List;

public class MockInternalHostApplication implements InternalHostApplication
{
    private List<EntityReference> entityReferences;

    public ApplicationId getId()
    {
        throw new UnsupportedOperationException();
    }

    public URI getBaseUrl()
    {
        throw new UnsupportedOperationException();
    }

    public URI getIconUrl()
    {
        throw new UnsupportedOperationException();
    }

    public URI getDocumentationBaseUrl()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        throw new UnsupportedOperationException();
    }

    public ApplicationType getType()
    {
        throw new UnsupportedOperationException();
    }

    public Iterable<Class<? extends AuthenticationProvider>> getSupportedInboundAuthenticationTypes()
    {
        throw new UnsupportedOperationException();
    }

    public Iterable<Class<? extends AuthenticationProvider>> getSupportedOutboundAuthenticationTypes()
    {
        throw new UnsupportedOperationException();
    }

    public EntityReference toEntityReference(final Object domainObject)
    {
        throw new UnsupportedOperationException();
    }

    public Iterable<EntityReference> getLocalEntities()
    {
        return entityReferences;
    }

    public boolean doesEntityExist(final String key, final Class<? extends EntityType> type)
    {
        throw new UnsupportedOperationException();
    }

    public boolean doesEntityExistNoPermissionCheck(final String key, final Class<? extends EntityType> type)
    {
        throw new UnsupportedOperationException();
    }

    public EntityReference toEntityReference(final String key, final Class<? extends EntityType> type)
    {
        throw new UnsupportedOperationException();
    }

    public boolean canManageEntityLinksFor(final EntityReference entityReference)
    {
        throw new UnsupportedOperationException();
    }

    public void setEntityReferences(final List<EntityReference> entityReferences)
    {
        this.entityReferences = entityReferences;
    }

    public boolean hasPublicSignup() {
        return false;
    }
}
