package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.RenderingContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;

import static com.google.common.collect.Iterables.transform;

/**
 * Common functions for default admin web items implementations.
 *
 * @since 1,0
 */
public final class DefaultAdminWebItems
{

    private DefaultAdminWebItems()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static Function<WebItemModuleDescriptor, AdminLink> toLink(final RenderingContext context)
    {
        return new Function<WebItemModuleDescriptor, AdminLink>()
        {
            @Override
            public AdminLink apply(WebItemModuleDescriptor descriptor)
            {
                return new DefaultAdminLink(descriptor, context);
            }
        };
    }

    public static Function<WebSectionModuleDescriptor, AdminLinkSection> toSection(final RenderingContext renderingContext,
                                                                                   final WebInterfaceManager webInterfaceManager)
    {
        return new Function<WebSectionModuleDescriptor, AdminLinkSection>()
        {
            @Override
            public AdminLinkSection apply(WebSectionModuleDescriptor descriptor)
            {
                return new DefaultAdminLinkSection(descriptor, renderingContext, webInterfaceManager);
            }
        };
    }

    public static Function<WebSectionModuleDescriptor, AdminLinkSection> toSection(final RenderingContext renderingContext,
                                                                                   final WebInterfaceManager webInterfaceManager,
                                                                                   @Nullable final Predicate<AdminLink> linkFilter,
                                                                                   @Nullable final Predicate<AdminLinkSection> sectionFilter)
    {
        return new Function<WebSectionModuleDescriptor, AdminLinkSection>()
        {
            @Override
            public AdminLinkSection apply(WebSectionModuleDescriptor descriptor)
            {
                return new DefaultAdminLinkSection(descriptor, renderingContext, webInterfaceManager, linkFilter, sectionFilter);
            }
        };
    }

    public static Supplier<Iterable<AdminLinkSection>> childSections(final String location,
                                                                     final RenderingContext userContext,
                                                                     final WebInterfaceManager webInterfaceManager,
                                                                     @Nullable final Predicate<AdminLink> linkFilter,
                                                                     @Nullable final Predicate<AdminLinkSection> sectionFilter)
    {
        return new Supplier<Iterable<AdminLinkSection>>()
        {
            @Override
            public Iterable<AdminLinkSection> get()
            {
                Iterable<AdminLinkSection> sections = ImmutableList.copyOf(transform(webInterfaceManager.getDisplayableSections(location,
                        userContext.getContextMap()), toSection(userContext, webInterfaceManager, linkFilter, sectionFilter)));
                return Iterables.filter(sections, sectionFilter != null ? sectionFilter : Predicates.<AdminLinkSection>alwaysTrue());
            }
        };
    }

    public static Supplier<Iterable<AdminLink>> childLinks(final String section,
                                                           final RenderingContext renderingContext,
                                                           final WebInterfaceManager webInterfaceManager,
                                                           @Nullable final Predicate<AdminLink> linkFilter)
    {
        return new Supplier<Iterable<AdminLink>>()
        {
            @Override
            public Iterable<AdminLink> get()
            {
                Iterable<AdminLink> links = ImmutableList.copyOf(transform(webInterfaceManager.getDisplayableItems(section,
                        renderingContext.getContextMap()), toLink(renderingContext)));
                return Iterables.filter(links, linkFilter != null ? linkFilter : Predicates.<AdminLink>alwaysTrue());
            }
        };
    }

    public static Supplier<Iterable<AdminLinkSection>> childSections(final String location,
                                                                     final RenderingContext userContext,
                                                                     final WebInterfaceManager webInterfaceManager)
    {
        return childSections(location, userContext, webInterfaceManager, null, null);
    }

    public static Supplier<Iterable<AdminLink>> childLinks(final String section,
                                                           final RenderingContext renderingContext,
                                                           final WebInterfaceManager webInterfaceManager)
    {
        return childLinks(section, renderingContext, webInterfaceManager, null);
    }
}
