package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkRequestFactory;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;

import java.net.URI;

public class MockApplicationLink implements ApplicationLink
{
    public ApplicationId getId()
    {
        throw new UnsupportedOperationException();
    }

    public ApplicationType getType()
    {
        throw new UnsupportedOperationException();
    }

    public String getName()
    {
        throw new UnsupportedOperationException();
    }

    public URI getDisplayUrl()
    {
        throw new UnsupportedOperationException();
    }

    public URI getIconUrl()
    {
        throw new UnsupportedOperationException();
    }

    public URI getRpcUrl()
    {
        throw new UnsupportedOperationException();
    }

    public boolean isPrimary()
    {
        throw new UnsupportedOperationException();
    }

    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory()
    {
        throw new UnsupportedOperationException();
    }

    public ApplicationLinkRequestFactory createAuthenticatedRequestFactory(final Class<? extends AuthenticationProvider> providerClass)
    {
        throw new UnsupportedOperationException();
    }

    public Object getProperty(final String key)
    {
        throw new UnsupportedOperationException();
    }

    public Object putProperty(final String key, final Object value)
    {
        throw new UnsupportedOperationException();
    }

    public Object removeProperty(final String key)
    {
        throw new UnsupportedOperationException();
    }
}
