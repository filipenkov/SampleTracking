package com.atlassian.applinks.core.auth.oauth;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.TypeNotInstalledException;
import com.atlassian.applinks.api.auth.types.OAuthAuthenticationProvider;
import com.atlassian.applinks.core.auth.OrphanedTrustCertificate;
import com.atlassian.applinks.core.auth.OrphanedTrustDetector;
import com.atlassian.applinks.core.auth.oauth.servlets.consumer.AddServiceProviderManuallyServlet;
import com.atlassian.applinks.spi.auth.AuthenticationConfigurationManager;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.serviceprovider.ServiceProviderConsumerStore;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Finds orphaned OAuth consumers (that is, they are stored locally but are not related to a
 * configured {@link ApplicationLink})
 *
 * @since 3.0
 */
public class OAuthOrphanedTrustDetector implements OrphanedTrustDetector
{

    private final ApplicationLinkService applicationLinkService;
    private final ServiceProviderConsumerStore serviceProviderConsumerStore;
    private final ServiceProviderStoreService serviceProviderStoreService;
    private final ConsumerService consumerService;
    private final AuthenticationConfigurationManager authenticationConfigurationManager;
    private static final Logger log = LoggerFactory.getLogger(OAuthOrphanedTrustDetector.class);

    public OAuthOrphanedTrustDetector(final ApplicationLinkService applicationLinkService,
                                      final ServiceProviderConsumerStore serviceProviderConsumerStore,
                                      final ServiceProviderStoreService serviceProviderStoreService,
                                      final ConsumerService consumerService,
                                      final AuthenticationConfigurationManager authenticationConfigurationManager)
    {
        this.applicationLinkService = applicationLinkService;
        this.serviceProviderConsumerStore = serviceProviderConsumerStore;
        this.serviceProviderStoreService = serviceProviderStoreService;
        this.consumerService = consumerService;
        this.authenticationConfigurationManager = authenticationConfigurationManager;
    }

    public List<OrphanedTrustCertificate> findOrphanedTrustCertificates()
    {
        final List<OrphanedTrustCertificate> orphanedTrustCertificates = new ArrayList<OrphanedTrustCertificate>();

        orphanedTrustCertificates.addAll(findOrphanedOAuthConsumers());
        orphanedTrustCertificates.addAll(findOrphanedOAuthServiceProviders());
        return orphanedTrustCertificates;
    }

    private List<OrphanedTrustCertificate> findOrphanedOAuthServiceProviders()
    {
        final List<OrphanedTrustCertificate> orphanedTrustCertificates = new ArrayList<OrphanedTrustCertificate>();
        final List<String> registeredServiceProviders = findRegisteredServiceProviders();
        final Iterable<Consumer> allServiceProviders = consumerService.getAllServiceProviders();
        for (Consumer serviceProvider : allServiceProviders)
        {
            if (!registeredServiceProviders.contains(serviceProvider.getKey()))
            {
                log.debug("Found orphaned Service Provider with consumer key '" + serviceProvider.getKey() + "' and name '" + serviceProvider.getName() + "'");
                orphanedTrustCertificates.add(
                        new OrphanedTrustCertificate(serviceProvider.getKey(), serviceProvider.getDescription(),
                                OrphanedTrustCertificate.Type.OAUTH_SERVICE_PROVIDER)
                );
            }
        }
        return orphanedTrustCertificates;
    }

    private List<String> findRegisteredServiceProviders()
    {
        final List<String> serviceProviderConsumerKeys = new ArrayList<String>();
        for (final ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            if (authenticationConfigurationManager.isConfigured(link.getId(), OAuthAuthenticationProvider.class))
            {
                Map<String, String> configuration = authenticationConfigurationManager.getConfiguration(link.getId(), OAuthAuthenticationProvider.class);
                final String consumerKey = configuration.get(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND);
                serviceProviderConsumerKeys.add(consumerKey);
            }
        }
        return serviceProviderConsumerKeys;
    }


    private List<OrphanedTrustCertificate> findOrphanedOAuthConsumers()
    {
        final List<OrphanedTrustCertificate> orphanedTrustCertificates = new ArrayList<OrphanedTrustCertificate>();
        final Set<String> recognisedConsumerKeys = new HashSet<String>();
        for (final ApplicationLink link : applicationLinkService.getApplicationLinks())
        {
            final Consumer consumer = serviceProviderStoreService.getConsumer(link);
            if (consumer != null)
            {
                recognisedConsumerKeys.add(consumer.getKey());
            }
        }

        for (final Consumer consumer : serviceProviderConsumerStore.getAll())
        {
            if (!recognisedConsumerKeys.contains(consumer.getKey()))
            {
                orphanedTrustCertificates.add(
                        new OrphanedTrustCertificate(consumer.getKey(), consumer.getDescription(),
                                OrphanedTrustCertificate.Type.OAUTH)
                );
            }
        }

        return orphanedTrustCertificates;
    }

    public void deleteTrustCertificate(final String id, final OrphanedTrustCertificate.Type type)
    {
        checkCertificateType(type);
        if (type == OrphanedTrustCertificate.Type.OAUTH)
        {
            serviceProviderConsumerStore.remove(id);
        }
        else if (type == OrphanedTrustCertificate.Type.OAUTH_SERVICE_PROVIDER)
        {
            consumerService.removeConsumerByKey(id);
        }
    }

    private void checkCertificateType(final OrphanedTrustCertificate.Type type)
    {
        if (type != OrphanedTrustCertificate.Type.OAUTH && type != OrphanedTrustCertificate.Type.OAUTH_SERVICE_PROVIDER)
        {
            throw new IllegalArgumentException("Unsupported type: " + type);
        }
    }

    public void addOrphanedTrustToApplicationLink(final String id, final OrphanedTrustCertificate.Type type, final ApplicationId applicationId)
    {
        checkCertificateType(type);
        final ApplicationLink applicationLink;
        try
        {
            applicationLink = applicationLinkService.getApplicationLink(applicationId);
            if (applicationLink == null)
            {
                throw new RuntimeException("No Application Link with id '" + applicationId + "' found.");
            }
        }
        catch (TypeNotInstalledException e)
        {
            throw new IllegalStateException("An application of the type " + e.getType() + " is not installed!", e);
        }

        if (type == OrphanedTrustCertificate.Type.OAUTH)
        {
            registerOAuthConsumer(id, applicationLink);
        }
        else if (type == OrphanedTrustCertificate.Type.OAUTH_SERVICE_PROVIDER)
        {
            registerOAuthServiceProvider(id, applicationLink);
        }
    }

    private void registerOAuthServiceProvider(final String id, final ApplicationLink applicationLink)
    {
        final Consumer consumer = consumerService.getConsumerByKey(id);
        final String requestTokenUrl = applicationLink.getRpcUrl() + "/request/token";
        final String accessTokenUrl = applicationLink.getRpcUrl() + "/access/token";
        final String authorizeUrl = applicationLink.getDisplayUrl() + "/authorize/token";

        authenticationConfigurationManager.registerProvider(
                applicationLink.getId(),
                OAuthAuthenticationProvider.class,
                ImmutableMap.of(AddServiceProviderManuallyServlet.CONSUMER_KEY_OUTBOUND, consumer.getKey(),
                        AddServiceProviderManuallyServlet.SERVICE_PROVIDER_REQUEST_TOKEN_URL, requestTokenUrl,
                        AddServiceProviderManuallyServlet.SERVICE_PROVIDER_ACCESS_TOKEN_URL, accessTokenUrl,
                        AddServiceProviderManuallyServlet.SERVICE_PROVIDER_AUTHORIZE_URL, authorizeUrl));
        log.debug("Associated OAuth ServiceProvider with consumer key '" + consumer.getKey() + "' with Application Link id='" + applicationLink.getId() + "' and name='" + applicationLink.getName() + "'");
    }

    private void registerOAuthConsumer(final String id, final ApplicationLink applicationLink)
    {
        Consumer consumer = serviceProviderConsumerStore.get(id);
        if (consumer == null)
        {
            throw new RuntimeException("No consumer with key '" + consumer.getKey() + "' registered!");
        }
        serviceProviderStoreService.addConsumer(consumer, applicationLink);
        log.debug("Associated OAuth Consumer with key '" + consumer.getKey() + "' with Application Link id='" + applicationLink.getId() + "' and name='" + applicationLink.getName() + "'");
    }
}
