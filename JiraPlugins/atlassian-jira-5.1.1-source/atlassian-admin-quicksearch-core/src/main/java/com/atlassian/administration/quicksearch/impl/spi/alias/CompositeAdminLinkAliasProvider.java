package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Set;

/**
 * {@link com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider} that delegates to a list
 * of providers and returns their collective response.
 *
 * @since 1.0
 */
public class CompositeAdminLinkAliasProvider implements AdminLinkAliasProvider
{

    private final Iterable<AdminLinkAliasProvider> providers;

    public CompositeAdminLinkAliasProvider(Iterable<AdminLinkAliasProvider> providers)
    {
        this.providers = providers;
    }

    @Override
    public Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext)
    {
        ImmutableSortedSet.Builder<String> answer = ImmutableSortedSet.naturalOrder();
        for (AdminLinkAliasProvider provider : providers)
        {
            answer.addAll(provider.getAliases(link, parentSections, userContext));
        }
        return answer.build();
    }
}
