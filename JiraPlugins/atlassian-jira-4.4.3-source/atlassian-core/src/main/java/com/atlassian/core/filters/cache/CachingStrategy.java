package com.atlassian.core.filters.cache;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * A strategy for the {@link AbstractCachingFilter}.
 * <p/>
 * If an implementation returns true from {@link #matches(HttpServletRequest)}, the filter will call
 * {@link #setCachingHeaders(HttpServletResponse)} for the strategy to apply the relevant caching headers.
 *
 * @see AbstractCachingFilter
 * @since 4.0
 */
public interface CachingStrategy
{
    /**
     * Returns true if the given request should be handled by this caching strategy.
     */
    boolean matches(HttpServletRequest request);

    /**
     * Sets the relevant caching headers for the response.
     */
    void setCachingHeaders(HttpServletResponse response);
}
