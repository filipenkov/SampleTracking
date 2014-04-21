package com.atlassian.administration.quicksearch.spi;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Provides data required to render web items.
 *
 * @since 1.0
 */
public interface RenderingContext
{

    /**
     * Get current executing request.
     *
     * @return HTTP request
     */
    @Nonnull
    HttpServletRequest getRequest();

    /**
     * Get the rendering context map.
     *
     * @return context map
     */
    @Nonnull
    Map<String,Object> getContextMap();
}
