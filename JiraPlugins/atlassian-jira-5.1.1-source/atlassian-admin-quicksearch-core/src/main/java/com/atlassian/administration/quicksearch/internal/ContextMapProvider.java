package com.atlassian.administration.quicksearch.internal;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Injects additional, app specific data into the context map.
 *
 * @since 1.0
 */
public interface ContextMapProvider
{

    /**
     * Add context to the existing context.
     *
     * @param existingContext existing context as an immutable map
     * @param request request
     * @return new context values (may or may not contain the original values)
     */
    Map<String,Object> addContextTo(Map<String,Object> existingContext, HttpServletRequest request);
}
