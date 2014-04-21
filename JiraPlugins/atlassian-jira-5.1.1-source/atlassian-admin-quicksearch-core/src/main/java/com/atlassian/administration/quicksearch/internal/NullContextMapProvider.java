package com.atlassian.administration.quicksearch.internal;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Map;

/**
 * Provides empty context map.
 *
 * @since 1.0
 */
public final class NullContextMapProvider implements ContextMapProvider
{
    public static final NullContextMapProvider INSTANCE = new NullContextMapProvider();

    @Override
    public Map<String, Object> addContextTo(Map<String, Object> existingContext, HttpServletRequest request)
    {
        return Collections.emptyMap();
    }
}
