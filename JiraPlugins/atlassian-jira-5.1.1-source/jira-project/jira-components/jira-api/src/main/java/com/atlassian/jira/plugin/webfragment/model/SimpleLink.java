package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.jira.util.NotNull;

/**
 * A simple link representation.
 *
 * @since v4.0
 */
public interface SimpleLink extends SimpleLinkSection
{
    /**
     * The URL that the link points to.  This should never be null.
     *
     * @return The URL that the link points to
     */
    @NotNull
    String getUrl();

    /**
     * The access key used to quickly select link
     *
     * @return The access key used to quickly select link
     */
    String getAccessKey();

}
