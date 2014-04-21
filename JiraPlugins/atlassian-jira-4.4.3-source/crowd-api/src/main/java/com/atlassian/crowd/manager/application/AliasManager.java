package com.atlassian.crowd.manager.application;

import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;

/**
 * API to manage application-specific user aliases.
 */
public interface AliasManager
{
    /**
     * Will return the 'real' username of the authenticating user.
     *
     * This returned username maybe the username passed in, if the user does not have a configured 'alias' for the given application
     *
     * @param application the application the user is associated with
     * @param authenticatingUsername the username passed to the application for authentication
     * @return The 'real' username of the authenticating user, or the passed in username
     * @throws IllegalArgumentException if either application or authenticatingUsername are null
     */
    String findUsernameByAlias(final Application application, final String authenticatingUsername);

    /**
     * Will return the 'alias' associated to the user.
     *
     * If an alias does not exist the 'real' username will be returned
     *
     * @param application the application the user is associated with
     * @param username the 'real' username of the user
     * @return if an alias exists it will be returned, otherwise the username will be returned.
     * @throws IllegalArgumentException if either application or authenticatingUsername are null
     */
    String findAliasByUsername(final Application application, final String username);

    /**
     * Perform an {@link com.atlassian.crowd.search.query.entity.AliasQuery} search on the alias tables of Crowd.
     *
     * @param entityQuery Will accept an entity query for aliases as an {@link com.atlassian.crowd.search.query.entity.AliasQuery}
     * @return a {@link java.util.List<String>} of usernames for a given {@link com.atlassian.crowd.search.query.entity.AliasQuery} or an empty list 
     */
    List<String> search(final EntityQuery entityQuery);
    
    /**
     * Add or update the application-specific alias for a username.
     *
     * @param application application context.
     * @param username real username of user in directory.
     * @param alias application-specific alias (cannot be <code>null</code> or blank).
     * @throws IllegalArgumentException if parameters are <code>null</code> or blank.
     * @throws AliasAlreadyInUseException the alias is already in use by another user for the given application.
     */
    void storeAlias(Application application, String username, String alias) throws AliasAlreadyInUseException;

    /**
     * Remove an application-specific alias for a username.
     *
     * This method will silently succeed if the username has no application-specific alias.
     *
     * @param application application context.
     * @param username real username of user in directory.
     * @throws AliasAlreadyInUseException the username is already in use by another user as their alias for the given application.
     */
    void removeAlias(Application application, String username) throws AliasAlreadyInUseException;
}
