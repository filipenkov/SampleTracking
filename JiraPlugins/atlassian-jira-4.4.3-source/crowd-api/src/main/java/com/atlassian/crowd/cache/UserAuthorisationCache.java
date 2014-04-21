package com.atlassian.crowd.cache;

/**
 * Cache to determine whether a user is authorised to authenticate with an application.
 *
 * @since v2.2
 */
public interface UserAuthorisationCache
{
    /**
     * Sets whether the user is permitted to authenticate with the application.
     *
     * @param userName username
     * @param applicationName name of the application to authenticate
     * @param permitted set to <tt>true</tt> if the user is allowed to authenticate with the application, otherwise false.
     */
    void setPermitted(String userName, String applicationName, boolean permitted);

    /**
     * Returns whether the user is permitted to authenticate with the application.
     *
     * @param userName username
     * @param applicationName name of the application the user is authenticating against
     * @return <tt>true</tt> if the user is permitted to authenticate with the application, <tt>false</tt> if the user
     *          is not permitted to authenticate, and <tt>null</tt> if the result is not in the cache.
     */
    Boolean isPermitted(String userName, String applicationName);

    /**
     * Clears the user authorisation cache.
     */
    void clear();
}
