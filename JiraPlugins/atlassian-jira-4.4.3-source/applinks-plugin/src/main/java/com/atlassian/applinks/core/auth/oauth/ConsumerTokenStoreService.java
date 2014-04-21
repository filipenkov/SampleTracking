package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.oauth.consumer.ConsumerToken;

/**
 * The ConsumerTokenStoreService is a wrapper around the {@link com.atlassian.oauth.consumer.ConsumerTokenStore} and
 * {@link  com.atlassian.oauth.consumer.ConsumerService}, it takes care of adding / removing / retrieving consumer tokens.
 *
 * @since 3.0
 */
public interface ConsumerTokenStoreService
{
    /**
     * Persists an obtained access token. The actual persistence happens inside the {@link com.atlassian.oauth.consumer.ConsumerTokenStore}.
     * If the application referenced by the {@code applicationLink} is an atlassian application it stores the application Id in the consumer token properties.
     *
     * @param applicationLink the applicationlink this token is valid for.
     * @param username        the user who obtained this token.
     * @param consumerToken   the (access) token that has been obtained.
     */
    void addConsumerToken(ApplicationLink applicationLink, String username, ConsumerToken consumerToken);

    /**
     * Removes all obtained {@link com.atlassian.oauth.consumer.ConsumerToken}s for a given application link.
     *
     * @param applicationLink
     */
    void removeAllConsumerTokens(ApplicationLink applicationLink);

    /**
     * Removes the consumer token of a user for an application link.
     *
     * @param applicationId the id of the application link.
     * @param username the username of the user.
     *
     * @return true if the token got deleted or false if no token was present for this user.
     */
    boolean removeConsumerToken(ApplicationId applicationId, final String username);

    /**
     * Retrieves a {@link ConsumerToken} for a given application link and user.
     *
     * @param applicationLink the application link the token has to be valid for.
     * @param username the user who wants to use this token.
     * @return an (access) token for the given user and application or null if no token present.
     */
    ConsumerToken getConsumerToken(ApplicationLink applicationLink, String username);

}
