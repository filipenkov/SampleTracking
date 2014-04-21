package com.atlassian.crowd.integration.rest.service.factory;

import com.atlassian.crowd.integration.rest.service.RestCrowdClient;
import com.atlassian.crowd.service.client.ClientProperties;
import com.atlassian.crowd.service.client.ClientPropertiesImpl;
import com.atlassian.crowd.service.client.CrowdClient;
import com.atlassian.crowd.service.factory.CrowdClientFactory;
import org.apache.commons.lang.StringUtils;

import java.util.Properties;

/**
 * Factory class for creating a new instance of CrowdClient using REST.
 */
public class RestCrowdClientFactory implements CrowdClientFactory
{
    public CrowdClient newInstance(final String url, final String applicationName, final String applicationPassword)
    {
        final ClientProperties clientProperties = new RestClientProperties(url, applicationName, applicationPassword);

        return newInstance(clientProperties);
    }

    public CrowdClient newInstance(final ClientProperties clientProperties)
    {
        return new RestCrowdClient(clientProperties);
    }

    /**
     * This class is used for forcing the use of specified url, application
     * name and application password. Values in ClientPropertiesImpl can
     * be overridden using system properties.
     */
    private static class RestClientProperties extends ClientPropertiesImpl
    {
        private final String baseURL;
        private final String applicationName;
        private final String applicationPassword;

        RestClientProperties(final String url, final String applicationName, final String applicationPassword)
        {
            this.baseURL = StringUtils.removeEnd(url, "/");
            this.applicationName = applicationName;
            this.applicationPassword = applicationPassword;
            updateProperties(new Properties());
        }

        @Override
        public String getBaseURL()
        {
            return baseURL;
        }

        @Override
        public String getApplicationName()
        {
            return applicationName;
        }

        @Override
        public String getApplicationPassword()
        {
            return applicationPassword;
        }
    }
}
