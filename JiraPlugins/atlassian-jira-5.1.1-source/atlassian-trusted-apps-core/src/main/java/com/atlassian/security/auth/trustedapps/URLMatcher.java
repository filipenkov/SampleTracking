package com.atlassian.security.auth.trustedapps;

/**
 * Checks whether the supplied url path (after the context) is valid.
 */
public interface URLMatcher
{
    /**
     * Check if this URL path is allowed to be accessed by trusted calls
     */
    boolean match(String urlPath);
}