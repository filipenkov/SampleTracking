package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AliasProviderConfiguration;

/**
 * Implementation of {@link AliasProviderConfiguration}
 * as a simple bean. Useful for certain tricks in DI/OSGi containers.
 *
 * @since 1.0
 */
public class DefaultAliasProviderConfiguration implements AliasProviderConfiguration
{

    private final AdminLinkAliasProvider provider;

    public DefaultAliasProviderConfiguration(AdminLinkAliasProvider provider)
    {
        this.provider = provider;
    }

    /**
     * To enable usage of multiple providers.
     *
     * @param providers providers to use
     */
    public DefaultAliasProviderConfiguration(Iterable<AdminLinkAliasProvider> providers)
    {
        this.provider = new CompositeAdminLinkAliasProvider(providers);
    }

    @Override
    public AdminLinkAliasProvider getAliasProvider()
    {
        return provider;
    }
}
