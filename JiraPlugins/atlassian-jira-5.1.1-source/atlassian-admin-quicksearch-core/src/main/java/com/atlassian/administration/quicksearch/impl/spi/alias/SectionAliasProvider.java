package com.atlassian.administration.quicksearch.impl.spi.alias;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.google.common.collect.ImmutableSortedSet;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * Provides the link's section names as its aliases.
 *
 * @since 1.0
 */
public class SectionAliasProvider implements AdminLinkAliasProvider
{
    @Override
    public Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext)
    {
        ImmutableSortedSet.Builder<String> setBuilder = ImmutableSortedSet.naturalOrder();
        for (AdminLinkSection section : parentSections)
        {
            if (isNotBlank(section.getLabel()))
            {
                setBuilder.add(section.getLabel());
            }
        }
        return setBuilder.build();
    }
}
