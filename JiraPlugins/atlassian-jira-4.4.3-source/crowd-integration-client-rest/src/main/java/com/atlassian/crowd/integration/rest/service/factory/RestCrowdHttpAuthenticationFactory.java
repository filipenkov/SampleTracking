package com.atlassian.crowd.integration.rest.service.factory;

import com.atlassian.crowd.integration.Constants;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticator;
import com.atlassian.crowd.integration.http.CrowdHttpAuthenticatorImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelper;
import com.atlassian.crowd.integration.http.util.CrowdHttpTokenHelperImpl;
import com.atlassian.crowd.integration.http.util.CrowdHttpValidationFactorExtractorImpl;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.ClientResourceLocator;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;

/**
 * This factory can be used to access a lazily instantiated singleton instance
 * of CrowdHttpAuthenticator.
 */
public class RestCrowdHttpAuthenticationFactory
{
    private final static CrowdHttpAuthenticator crowdHttpAuthenticator = createInstance();

    private RestCrowdHttpAuthenticationFactory()
    {
        // Not to be instantiated.
    }

    /**
     * Returns a singleton instance of CrowdHttpAuthenticator.
     *
     * @return singleton instance of CrowdHttpAuthenticator
     */
    public static CrowdHttpAuthenticator getAuthenticator()
    {
        return crowdHttpAuthenticator;
    }

    private static CrowdHttpAuthenticator createInstance()
    {
        final ClientResourceLocator clientResourceLocator = new ClientResourceLocator(Constants.PROPERTIES_FILE);
        final ClientProperties clientProperties = ClientPropertiesImpl.newInstanceFromResourceLocator(clientResourceLocator);
        final CrowdClientFactory clientFactory = new RestCrowdClientFactory();
        final CrowdClient crowdClient = clientFactory.newInstance(clientProperties);
        final CrowdHttpTokenHelper tokenHelper = CrowdHttpTokenHelperImpl.getInstance(CrowdHttpValidationFactorExtractorImpl.getInstance());
        return new CrowdHttpAuthenticatorImpl(crowdClient, clientProperties, tokenHelper);
    }
}
