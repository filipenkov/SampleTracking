package com.atlassian.administration.quicksearch.spi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

/**
 * Base interface for admin sections and links.
 *
 * @since 1.0
 */
public interface AdminWebItem
{
    /**
     * Unique ID of the web item. Can be <code>null</code>, e.g. if an item (link, section) is dynamically generated.
     *
     * @return ID of the link
     */
    @Nullable
    String getId();

    /**
     * Human readable label of the item. Can be <code>null</code>.
     *
     * @return label of the item
     */
    @Nullable
    String getLabel();

    /**
     * Custom parameters configured for this web item
     *
     * @return map of custom parameters
     */
    @Nonnull
    Map<String,String> getParameters();
}
