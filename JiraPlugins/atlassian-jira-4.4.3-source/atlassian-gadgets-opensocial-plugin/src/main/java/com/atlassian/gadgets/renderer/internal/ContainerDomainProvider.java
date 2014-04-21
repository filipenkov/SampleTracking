package com.atlassian.gadgets.renderer.internal;

import java.net.URI;

import com.atlassian.sal.api.ApplicationProperties;

/**
 * Provides the domain name to be used for creating security tokens.
 */
public class ContainerDomainProvider
{
    private final ApplicationProperties applicationProperties;

    public ContainerDomainProvider(ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Returns the domain as it appears in the applications base URL.
     * 
     * @return the domain as it appears in the applications base URL
     */
    public String getDomain()
    {
        return URI.create(applicationProperties.getBaseUrl()).getHost();
    }
}
