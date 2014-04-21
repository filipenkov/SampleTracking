package com.atlassian.administration.quicksearch.spi;

import java.util.Set;

/**
 * Responsible for providing aliases for admin links.
 *
 * @since 1.0
 */
public interface AdminLinkAliasProvider
{

    /**
     * Provide aliases for given link in given context
     *
     * @param link link
     * @param parentSections list of parent sections, starting from the top down to the direct parent of the link
     * @param userContext user context
     * @return set of aliases for the link in given context, may be empty but cannot be <code>null</code>
     */
    Set<String> getAliases(AdminLink link, Iterable<AdminLinkSection> parentSections, UserContext userContext);

}
