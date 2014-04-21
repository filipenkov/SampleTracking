package com.atlassian.jira.functest.framework.jerseyclient;

import com.sun.jersey.api.client.Client;

/**
 * Abstract factory for building Jersey clients.
 *
 * @since v4.3
 */
public interface JerseyClientFactory
{
    /**
     * Creates a Jersey client.
     *
     * @return a new Client instance
     */
    Client create();
}
