package com.atlassian.gadgets;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides methods for constructing {@code GadgetRequestContext} instances
 * from various sources.
 */
public interface GadgetRequestContextFactory
{
    /**
     * Returns a {@code GadgetRequestContext} based on the values in
     * the specified {@code request}.
     * @param request the request to look up the values in
     * @return a {@code GadgetRequestContext} based on the request
     */
    GadgetRequestContext get(HttpServletRequest request);
}
