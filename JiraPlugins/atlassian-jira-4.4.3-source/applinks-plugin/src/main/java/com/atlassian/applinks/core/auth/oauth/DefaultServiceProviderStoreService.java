package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;

import static com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AbstractConsumerServlet.OAUTH_INCOMING_CONSUMER_KEY;

/**
 * @since 3.0
 */
public class DefaultServiceProviderStoreService implements ServiceProviderStoreService
{
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final ServiceProviderTokenStore serviceProviderTokenStore;

    public DefaultServiceProviderStoreService(final ServiceProviderConsumerStore serviceProviderConsumerStore, final ServiceProviderTokenStore serviceProviderTokenStore)
    {
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.serviceProviderTokenStore = serviceProviderTokenStore;
    }

    public void addConsumer(final Consumer consumer, final ApplicationLink applicationLink)
    {
        // don't check whether the consumer exists already - transparently upgrade existing OAuth consumers to UAL
        serviceProviderConsumerStore.put(consumer);
        applicationLink.putProperty(OAUTH_INCOMING_CONSUMER_KEY, consumer.getKey());
    }

    private String getConsumerKey(final ApplicationLink applicationLink)
    {
        final Object storedConsumerKey = applicationLink.getProperty(OAUTH_INCOMING_CONSUMER_KEY);
        if (storedConsumerKey != null)
        {
            return storedConsumerKey.toString();

        }
        return null;
    }

    public void removeConsumer(final ApplicationLink applicationLink)
    {
        final String consumerKey = getConsumerKey(applicationLink);
        if (consumerKey == null)
        {
            throw new IllegalStateException("No consumer configured for application link '" + applicationLink + "'.");
        }
        serviceProviderTokenStore.removeByConsumer(consumerKey);
        serviceProviderConsumerStore.remove(consumerKey);
        if (applicationLink.removeProperty(OAUTH_INCOMING_CONSUMER_KEY) == null)
        {
            throw new IllegalStateException("Failed to remove consumer with key '" + consumerKey + "' from application link '" + applicationLink + "'");
        }
    }

    public Consumer getConsumer(final ApplicationLink applicationLink)
    {
        final String consumerKey = getConsumerKey(applicationLink);
        if (consumerKey != null)
        {
            return serviceProviderConsumerStore.get(consumerKey);
        }
        return null;
    }

}
