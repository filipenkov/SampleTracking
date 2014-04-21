package com.atlassian.jira.functest.framework.jerseyclient;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.apache.ApacheHttpClient;
import org.codehaus.jackson.jaxrs.JacksonJsonProvider;

/**
 * Factory for Jersey clients that use Apache HttpClient.
 *
 * @since v4.3
 */
public class ApacheClientFactoryImpl implements JerseyClientFactory
{
    /**
     * The configuration used for creating the Jersey client.
     */
    private final ClientConfig config;

    /**
     * Creates a ClientFactory with the default configuration, which uses Jackson as the JSON marshaller.
     */
    public ApacheClientFactoryImpl()
    {
        this(new DefaultClientConfig());
        config.getClasses().add(JacksonJsonProvider.class);
    }

    /**
     * Creates a ClientFactory with the provided configuration.
     *
     * @param config a ClientConfig
     */
    public ApacheClientFactoryImpl(ClientConfig config)
    {
        this.config = config;
    }

    /**
     * Creates a Jersey client.
     *
     * @return a new Client instance
     */
    @Override
    public Client create()
    {
        return ApacheHttpClient.create(config);
    }
}
