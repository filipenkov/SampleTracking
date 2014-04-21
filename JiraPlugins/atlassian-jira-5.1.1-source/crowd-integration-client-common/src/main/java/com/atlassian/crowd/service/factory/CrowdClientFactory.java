package com.atlassian.crowd.service.factory;

import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.CrowdClient;

/**
 * Class will create new instances of a CrowdClient.
 */
public interface CrowdClientFactory
{
    /**
     * Construct a new Crowd Client instance.
     *
     * @param url  URL of the remote Crowd server.
     * @param applicationName The application name of the connecting application.
     * @param applicationPassword The password of the connecting application.
     * @return new instance of CrowdClient
     */
    CrowdClient newInstance(final String url, final String applicationName, final String applicationPassword);

    /**
     * Constructs a new Crowd Client instance from the client properties.
     *
     * @param clientProperties all the properties needed to initialise the client.
     * @return new instance of CrowdClient
     */
    CrowdClient newInstance(final ClientProperties clientProperties);
}
