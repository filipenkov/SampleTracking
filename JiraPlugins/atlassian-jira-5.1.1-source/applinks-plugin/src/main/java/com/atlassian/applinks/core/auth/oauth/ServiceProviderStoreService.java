package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.oauth.Consumer;

/**
 * This is a wrapper around the {@link com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore} and the {@link
 * com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore}.
 *
 * This store will make all required changes to these two stores and the application link when add / removing a consumer.
 *
 * @since 3.0
 */
public interface ServiceProviderStoreService
{
    /**
     * Registers a consumer for a application link.
     * @param consumer the consumer to register
     * @param applicationLink the consumer will registered against this application link.
     *
     * @throws IllegalStateException if a consumer with this key already exists.
     */
    void addConsumer(Consumer consumer, ApplicationLink applicationLink) throws IllegalStateException;

    /**
     * Removes a consumer from an application link.
     * Consumer can no longer access the application. All tokens created by this consumer are invalidated.
     *
     * @param applicationLink the application link that has a consumer associated with it.
     */
    void removeConsumer(ApplicationLink applicationLink);

    /**
     * Returns the registered consumer for an application link.
     *
     * @param applicationLink the application link to read the consumer information from.
     * @return the consumer registered for the given application link. Can be null, if no consumer registered.
     */
    Consumer getConsumer(ApplicationLink applicationLink);
}
