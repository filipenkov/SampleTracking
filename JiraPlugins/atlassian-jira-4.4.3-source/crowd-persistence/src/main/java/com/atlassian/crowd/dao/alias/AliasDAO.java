package com.atlassian.crowd.dao.alias;

import com.atlassian.crowd.model.application.Application;
import com.atlassian.crowd.model.application.ApplicationImpl;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.List;

/**
 * Manages persistence of aliases.
 */
public interface AliasDAO
{
    /**
     * Will search for a List of Alias' based on a given {@link com.atlassian.crowd.search.query.entity.EntityQuery}
     *
     * @param entityQuery an entity query of type {@link com.atlassian.crowd.search.EntityDescriptor#alias()} 
     * @return a list of usernames or a Collections.emptyList(). Note: this does not return Alias objects or alias names (only real usernames).
     */
    List<String> search(EntityQuery<String> entityQuery);

    /**
     * Retrieves the application-specific alias for a username.
     *
     * @param application application context.
     * @param username real username of user in directory.
     * @return application-specific alias or <code>null</code> if there is no alias for the user/application.
     */
    String findAliasByUsername(Application application, String username);

    /**
     * Retrieves the real username for a user in a directory given their application-specific alias.
     *
     * @param application application context.
     * @param alias application-specific alias.
     * @return real username or <code>null</code> if there is no user with the supplied alias for the given application.
     */
    String findUsernameByAlias(Application application, String alias);

    /**
     * Add or update the application-specific alias for a username.
     *
     * @param application application context.
     * @param username real username of user in directory.
     * @param alias application-specific alias (cannot be <code>null</code> or blank).
     * @throws IllegalArgumentException if parameters are <code>null</code> or blank.
     */
    void storeAlias(Application application, String username, String alias);

    /**
     * Remove an application-specific alias for a username.
     *
     * This method will silently succeed if the username has no application-specific alias.
     *
     * @param application application context.
     * @param username real username of user in directory.
     */
    void removeAlias(Application application, String username);

    /**
     * Removes all the username-aliases for a specific application.
     *
     * @param application application context.
     */
    void removeAliases(Application application);
}
