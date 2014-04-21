package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.applinks.core.event.BeforeApplicationLinkDeletedEvent;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.atlassian.oauth.serviceprovider.ServiceProviderTokenStore;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import java.util.Map;

import static com.atlassian.applinks.core.auth.oauth.servlets.serviceprovider.AbstractConsumerServlet.OAUTH_INCOMING_CONSUMER_KEY;

/**
 * This event listener listens to the {@link com.atlassian.applinks.core.event.BeforeApplicationLinkDeletedEvent} event
 * and clears other oauth configuration stores when an application link gets deleted.
 *
 * @since 3.0
 */
public class OAuthConfigListener implements DisposableBean
{
    private final EventPublisher eventPublisher;
    private final ServiceProviderTokenStore serviceProviderTokenStore;
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final AuthenticationConfigurationManager configurationManager;
    private final ConsumerTokenStoreService consumerTokenStoreService;
    private final ConsumerService consumerService;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public OAuthConfigListener(final EventPublisher eventPublisher,
            final ServiceProviderTokenStore serviceProviderTokenStore,
            final ServiceProviderConsumerStore serviceProviderConsumerStore,
            final AuthenticationConfigurationManager configurationManager,
            final ConsumerTokenStoreService consumerTokenStoreService, final ConsumerService consumerService)
    {
        this.eventPublisher = eventPublisher;
        this.serviceProviderTokenStore = serviceProviderTokenStore;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.configurationManager = configurationManager;
        this.consumerTokenStoreService = consumerTokenStoreService;
        this.consumerService = consumerService;
        eventPublisher.register(this);
    }

    @EventListener
    public void onApplicationLinkDeleted(BeforeApplicationLinkDeletedEvent beforeApplicationLinkDeletedEvent)
    {
        final ApplicationLink applicationLink = beforeApplicationLinkDeletedEvent.getApplicationLink();
        final Object oConsumerKey = applicationLink.getProperty(OAUTH_INCOMING_CONSUMER_KEY);
        if (oConsumerKey != null)
        {
            final String consumerKey = oConsumerKey.toString();
            serviceProviderTokenStore.removeByConsumer(consumerKey);
            serviceProviderConsumerStore.remove(consumerKey);
            logger.debug("Unregistered consumer with key '{}' for deleted application link {}", oConsumerKey, applicationLink);
        }

        if (configurationManager.isConfigured(applicationLink.getId(), OAuthAuthenticationProvider.class))
        {
            final Map<String,String> configuration = configurationManager.getConfiguration(applicationLink.getId(), OAuthAuthenticationProvider.class);
            final String consumerKey = configuration.get(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND);
            if (!StringUtils.isEmpty(consumerKey))
            {
                consumerService.removeConsumerByKey(consumerKey);
                logger.debug("Unregistered service provider with consumer key '{}' for deleted application link {}", consumerKey, applicationLink);
            }
            consumerTokenStoreService.removeAllConsumerTokens(applicationLink);
            logger.debug("Removed token for deleted application link {}", applicationLink);
        }
    }

    public void destroy() throws Exception
    {
        eventPublisher.unregister(this);
    }
}
