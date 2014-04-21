package com.atlassian.administration.quicksearch.spi;

import javax.annotation.Nonnull;

/**
 * Represents a single administration link.
 *
 * @since 1.0
 */
public interface AdminLink extends AdminWebItem
{
    /**
     * Get URL of this admin link.
     *
     * @return URL of the link
     */
    @Nonnull
    String getLinkUrl();

}
