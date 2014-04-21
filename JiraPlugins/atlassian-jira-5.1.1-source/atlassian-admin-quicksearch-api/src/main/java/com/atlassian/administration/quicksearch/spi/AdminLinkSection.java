package com.atlassian.administration.quicksearch.spi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

/**
 * <p/>
 * An administration link section, which is really a collection of child sections and/or links.
 *
 * <p/>
 * Implementations of this class are meant to be used per request and are therefore not required to be thread safe.
 *
 * @since 1.0
 */
@NotThreadSafe
public interface AdminLinkSection extends AdminWebItem
{
    /**
     * Location of this section, which is usually ID of its parent section. May be <code>null</code> in case of
     * root sections.
     *
     * @return location of this section
     */
    @Nullable
    String getLocation();

    /**
     * Child sections of this section.
     *
     * @return child sections
     */
    @Nonnull
    Iterable<AdminLinkSection> getSections();

    /**
     * Links of this section.
     *
     * @return links
     */
    @Nonnull
    Iterable<AdminLink> getLinks();
}
