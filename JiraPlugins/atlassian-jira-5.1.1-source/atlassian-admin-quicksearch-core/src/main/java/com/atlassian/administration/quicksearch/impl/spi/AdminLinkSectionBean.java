package com.atlassian.administration.quicksearch.impl.spi;

import com.atlassian.administration.quicksearch.spi.AdminLink;
import com.atlassian.administration.quicksearch.spi.AdminLinkSection;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.Map;

/**
 * Implementation of {@link com.atlassian.administration.quicksearch.spi.AdminLinkSection} as a simple bean.
 *
 * @since 1.0
 */
public class AdminLinkSectionBean extends AbstractAdminWebItemBean implements AdminLinkSection
{
    private final String location;

    private final Supplier<Iterable<AdminLinkSection>> sectionSupplier;
    private final Supplier<Iterable<AdminLink>> linkSupplier;

    public AdminLinkSectionBean(String id, String label, Map<String, String> params, String location,
                                Iterable<AdminLinkSection> sections,
                                Iterable<AdminLink> links)
    {
        super(id, label, params);
        this.location = location;
        this.sectionSupplier = Suppliers.<Iterable<AdminLinkSection>>ofInstance(ImmutableList.copyOf(sections));
        this.linkSupplier = Suppliers.<Iterable<AdminLink>>ofInstance(ImmutableList.copyOf(links));
    }

    public AdminLinkSectionBean(String id, String label, Map<String, String> params, String location,
                                Supplier<Iterable<AdminLinkSection>> sections,
                                Supplier<Iterable<AdminLink>> links)
    {
        super(id, label, params);
        this.location = location;
        this.sectionSupplier = Suppliers.memoize(sections);
        this.linkSupplier = Suppliers.memoize(links);
    }

    @Override
    public String getLocation()
    {
        return location;
    }

    @Nonnull
    @Override
    public Iterable<AdminLinkSection> getSections()
    {
        return sectionSupplier.get();
    }

    @Nonnull
    @Override
    public Iterable<AdminLink> getLinks()
    {
        return linkSupplier.get();
    }
}
