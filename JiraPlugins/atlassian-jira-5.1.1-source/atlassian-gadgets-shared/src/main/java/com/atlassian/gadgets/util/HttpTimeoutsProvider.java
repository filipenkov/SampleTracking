package com.atlassian.gadgets.util;

import com.atlassian.sal.api.ApplicationProperties;

/**
 * Utility class for providing standardised timeout values to http params.
 */
public final class HttpTimeoutsProvider
{
    public static final String SOCKET_TIMEOUT_PROPERTY_KEY = "http.socket.timeout";
    public static final String CONNECTION_TIMEOUT_PROPERTY_KEY = "http.connection.timeout";
    
    public static final int DEFAULT_SOCKET_TIMEOUT_MS = 15000;
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    
    private final ApplicationProperties applicationProperties;

    public HttpTimeoutsProvider(final ApplicationProperties applicationProperties)
    {
        this.applicationProperties = applicationProperties;
    }

    /**
     * Intended to be used to set connection timeout values when building HTTPParams for used with HTTPClient.
     * Tries to get a system property with {#CONNECTION_TIMEOUT_PROPERTY_KEY}, then an application property
     * and falls back to {#DEFAULT_CONNECT_TIMEOUT_MS}.
     * 
     * @return int to use as connection timeout value
     */
    public int getConnectionTimeout()
    {
        return getIntProperty(CONNECTION_TIMEOUT_PROPERTY_KEY, DEFAULT_CONNECT_TIMEOUT_MS);
    }

    /**
     * Intended to be used to set socket timeout values when building HTTPParams for used with HTTPClient.
     * Tries to get a system property with {#SOCKET_TIMEOUT_PROPERTY_KEY}, then an application property
     * and falls back to {#DEFAULT_SOCKET_TIMEOUT_MS}.
     *
     * @return int to use as socket timeout value
     */
    public int getSocketTimeout()
    {
        return getIntProperty(SOCKET_TIMEOUT_PROPERTY_KEY, DEFAULT_SOCKET_TIMEOUT_MS);
    }
    
    private int getIntProperty(final String propertyKey, final int defaultValue)
    {
        final String applicationProperty = applicationProperties.getPropertyValue(propertyKey);

        Integer propertyValue = safeGetInt(System.getProperty(propertyKey));
        if (propertyValue == null)
        {
            propertyValue = safeGetInt(applicationProperty);
        }
        if (propertyValue == null)
        {
            propertyValue = defaultValue;
        }
        return propertyValue;
    }


    private Integer safeGetInt(final String intAsString)
    {
        try
        {
            return Integer.parseInt(intAsString);
        } 
        catch (NumberFormatException e)
        {
            return null;
        }
    }

}
