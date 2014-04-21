package com.atlassian.administration.quicksearch.rest;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkAliasProvider;
import com.atlassian.administration.quicksearch.spi.AdminLinkManager;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.UserContext;
import com.google.common.base.Function;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;

/**
 * Abstract REST resource to provide admin links.
 *
 * @since 1.0
 */
public abstract class AbstractAdminLinkResource
{

    protected final AdminLinkManager linkManager;
    protected final AdminLinkAliasProvider aliasProvider;

    protected AbstractAdminLinkResource(AdminLinkManager linkManager, AdminLinkAliasProvider aliasProvider)
    {
        this.linkManager = linkManager;
        this.aliasProvider = aliasProvider;
    }

    protected final LocationBean getLinksFor(String location, UserContext userContext)
    {
        return getLinksFor(location, userContext, true);
    }

    protected final LocationBean getLinksFor(String location, UserContext userContext, boolean stripRootSections)
    {
        return getLinksFor(Collections.singletonList(location), userContext, stripRootSections);
    }

    protected final LocationBean getLinksFor(Iterable<String> locations, final UserContext userContext)
    {
        return getLinksFor(locations, userContext, true);
    }

    protected final LocationBean getLinksFor(Iterable<String> locations, final UserContext userContext, boolean stripRootSections)
    {
        final Iterable<AdminLinkSection> rootSections = ImmutableList.copyOf(Iterables.transform(locations, new Function<String, AdminLinkSection>()
        {
            @Override
            public AdminLinkSection apply(String location)
            {
                return linkManager.getSection(location, userContext);
            }
        }));
        if (stripRootSections)
        {
            return toLocationBean(rootSections, userContext);
        }
        else
        {
            // don't flatten sections, just place them as children of location bean
            final Map<String,LinkBean> currentLinks = Maps.newHashMap();
            final List<SectionBean> convertedSections = ImmutableList.copyOf(transform(rootSections, new Function<AdminLinkSection, SectionBean>()
            {
                @Override
                public SectionBean apply(@Nullable AdminLinkSection section)
                {
                    return toSectionBean(section, Collections.<AdminLinkSection>emptyList(), userContext, currentLinks);
                }
            }));
            return new LocationBean(null, Collections.<LinkBean>emptyList(), convertedSections);
        }
    }

    private LocationBean toLocationBean(final Iterable<AdminLinkSection> rootSections, final UserContext userContext)
    {
        return toSectionBean(rootSections, Collections.<AdminLinkSection>emptyList(), userContext);
    }

    private SectionBean toSectionBean(Iterable<AdminLinkSection> sections, Iterable<AdminLinkSection> parentSections,
                                      final UserContext context)
    {
        final List<SectionBean> sectionBeans = Lists.newArrayList();
        final Map<String,LinkBean> currentLinks = Maps.newHashMap();
        for (AdminLinkSection section : sections)
        {
            sectionBeans.add(toSectionBean(section, parentSections, context, currentLinks));
        }
        final List<SectionBean> allSections = Lists.newArrayList();
        final List<LinkBean> allLinks = Lists.newArrayList();
        for (SectionBean directChild : sectionBeans)
        {
            allSections.addAll(directChild.sections());
            allLinks.addAll(directChild.links());
        }
        final String key = sectionBeans.size() == 1 ? sectionBeans.get(0).key() : null;
        final String label = sectionBeans.size() == 1 ? sectionBeans.get(0).label() : null;
        final String location = sectionBeans.size() == 1 ? sectionBeans.get(0).location() : null;
        return new SectionBean(key, location, label, allSections, allLinks);
    }

    private SectionBean toSectionBean(AdminLinkSection section, Iterable<AdminLinkSection> parentSections,
                                      final UserContext context, final Map<String,LinkBean> currentLinks)
    {
        final Iterable<AdminLinkSection> fullParents = Iterables.concat(parentSections, ImmutableList.of(section));
        ImmutableList.Builder<SectionBean> childSections = ImmutableList.builder();
        // convert links first - this affects where links with duplicate URLs will be put (top section first)
        List<LinkBean> links = transformAndCopy(section.getLinks(), new Function<AdminLink, LinkBean>()
        {
            public LinkBean apply(AdminLink link)
            {
                // don't return duplicate URLs, use aliases instead
                if (currentLinks.containsKey(link.getLinkUrl()))
                {
                    final LinkBean existing = currentLinks.get(link.getLinkUrl());
                    existing.aliases = ImmutableSortedSet.<String>naturalOrder().addAll(existing.aliases)
                            .add(link.getLabel()).addAll(aliasProvider.getAliases(link, fullParents, context)).build();
                    return null;
                }
                else
                {
                    final LinkBean newLink = new LinkBean(link.getId(), link.getLinkUrl(), link.getLabel(),
                            aliasProvider.getAliases(link, fullParents, context));
                    currentLinks.put(newLink.linkUrl, newLink);
                    return newLink;
                }
            }
        });
        for (AdminLinkSection childSection : section.getSections())
        {
            childSections.add(toSectionBean(childSection, fullParents, context, currentLinks));
        }
        return new SectionBean(section.getId(), section.getLabel(), section.getLocation(), childSections.build(), links);
    }

    private <I,O> List<O> transformAndCopy(Iterable<I> input, Function<I,O> transformer)
    {
        return ImmutableList.copyOf(filter(transform(input, transformer), Predicates.<O>notNull()));
    }


}
