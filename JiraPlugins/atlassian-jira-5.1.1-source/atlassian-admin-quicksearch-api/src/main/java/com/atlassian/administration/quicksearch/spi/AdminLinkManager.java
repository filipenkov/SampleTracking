package com.atlassian.administration.quicksearch.spi;

import javax.annotation.Nonnull;

/**
 * Provides admin sections for given location. Sections and links are ordered in a tree hierarchy, where a section
 * can contain any number of child sections and links and the child sections can again contain sections and links, and
 * so on...
 *
 * @since 1.0
 */
public interface AdminLinkManager
{

    /**
     * Return root section representing given location.
     *
     * @param location location to look into
     * @param userContext current user context
     * @return section representing root of the section/link tree, never <code>null</code>
     */
    @Nonnull
    AdminLinkSection getSection(String location, UserContext userContext);

}
