package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.atlassian.administration.quicksearch.spi.RenderingContext;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebSectionModuleDescriptor;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.atlassian.administration.quicksearch.impl.spi.SectionKeys.fullSectionKey;
import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Default implementation of {@link com.atlassian.administration.quicksearch.spi.AdminLinkSection}
 * based on the Atlassian web-fragments API.
 *
 * @since 1.0
 */
public class DefaultAdminLinkSection extends AbstractDefaultAdminWebItem<WebSectionModuleDescriptor>
        implements AdminLinkSection

{

    private final WebInterfaceManager webInterfaceManager;

    private final Predicate<AdminLink> linkFilter;
    private final Predicate<AdminLinkSection> sectionFilter;

    private Iterable<AdminLinkSection> sections;
    private Iterable<AdminLink> links;

    public DefaultAdminLinkSection(WebSectionModuleDescriptor descriptor, RenderingContext context,
                                   WebInterfaceManager webInterfaceManager)
    {
        this(descriptor, context, webInterfaceManager, null, null);
    }
     public DefaultAdminLinkSection(WebSectionModuleDescriptor descriptor, RenderingContext context,
                                   WebInterfaceManager webInterfaceManager,
                                   @Nullable Predicate<AdminLink> linkFilter,
                                   @Nullable Predicate<AdminLinkSection> sectionFilter)
    {
        super(descriptor, context);
        this.webInterfaceManager = checkNotNull(webInterfaceManager, "webInterfaceManager");
        this.linkFilter = linkFilter != null ? linkFilter : Predicates.<AdminLink>alwaysTrue();
        this.sectionFilter = sectionFilter != null ? sectionFilter : Predicates.<AdminLinkSection>alwaysTrue();
    }


    @Override
    public String getLocation()
    {
        return descriptor.getLocation();
    }

    @Nonnull
    @Override
    public Iterable<AdminLinkSection> getSections()
    {
        if (sections == null)
        {
            sections = DefaultAdminWebItems.childSections(getId(), context, webInterfaceManager, linkFilter, sectionFilter).get();
        }
        return sections;
    }

    @Nonnull
    @Override
    public Iterable<AdminLink> getLinks()
    {
        if (links == null)
        {
            links = DefaultAdminWebItems.childLinks(fullSectionKey(this), context, webInterfaceManager, linkFilter).get();
        }
        return links;
    }

}
