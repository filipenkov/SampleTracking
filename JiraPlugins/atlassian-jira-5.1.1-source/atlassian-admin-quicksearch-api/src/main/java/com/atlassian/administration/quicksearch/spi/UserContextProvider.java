package com.atlassian.administration.quicksearch.spi;

import javax.servlet.http.HttpServletRequest;

/**
 * Provides user context for given request
 *
 * @since 1.0
 */
public interface UserContextProvider
{

    /**
     * Get user context for given <tt>request</tt>.
     *
     * @param request current request
     * @return user context, or <code>null</code> if no user is associated with the <tt>request</tt>
     */
    UserContext getUserContext(HttpServletRequest request);
}
