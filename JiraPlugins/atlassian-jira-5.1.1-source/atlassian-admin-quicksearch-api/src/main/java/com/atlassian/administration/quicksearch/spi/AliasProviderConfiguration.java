package com.atlassian.administration.quicksearch.spi;

/**
 * Responsible for providing {@link com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider}.
 *
 * @since 1.0
 */
public interface AliasProviderConfiguration
{

    /**
     * Get {@link com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider} instance to use.
     *
     * @return link alias provider instance to use
     */
    AdminLinkAliasProvider getAliasProvider();
}
