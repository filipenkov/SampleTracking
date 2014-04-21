package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import org.apache.commons.lang.math.NumberUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Represents the LDAP connection pool properties which are set as system properties. Used in the UI plugin
 * to display the "Current Settings".
 */
public enum SystemConnectionPoolProperties implements ConnectionPoolProperties
{
    INSTANCE;

    public static ConnectionPoolProperties getInstance()
    {
        return INSTANCE;
    }

    private SystemConnectionPoolProperties()
    {
    }

    public String getInitialSize()
    {
        return System.getProperty(ConnectionPoolPropertyConstants.POOL_INITIAL_SIZE, ConnectionPoolPropertyConstants.DEFAULT_INITIAL_POOL_SIZE);
    }

    public String getMaximumSize()
    {
        return System.getProperty(ConnectionPoolPropertyConstants.POOL_MAXIMUM_SIZE, ConnectionPoolPropertyConstants.DEFAULT_MAXIMUM_POOL_SIZE);
    }

    public String getPreferredSize()
    {
        return System.getProperty(ConnectionPoolPropertyConstants.POOL_PREFERRED_SIZE, ConnectionPoolPropertyConstants.DEFAULT_PREFERRED_POOL_SIZE);
    }

    public String getSupportedProtocol()
    {
        return System.getProperty(ConnectionPoolPropertyConstants.POOL_PROTOCOL, ConnectionPoolPropertyConstants.DEFAULT_POOL_PROTOCOL);
    }

    public String getTimeoutInSec()
    {
        // Stored as milliseconds, but display as seconds to the user
        return Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(System.getProperty(ConnectionPoolPropertyConstants.POOL_TIMEOUT, ConnectionPoolPropertyConstants.DEFAULT_POOL_TIMEOUT_MS)), TimeUnit.MILLISECONDS));
    }

    public String getSupportedAuthentication()
    {
        return System.getProperty(ConnectionPoolPropertyConstants.POOL_AUTHENTICATION, ConnectionPoolPropertyConstants.DEFAULT_POOL_AUTHENTICATION);
    }

    public Map<String, String> toPropertiesMap()
    {
        throw new UnsupportedOperationException("Should never reapply system property values.");
    }
}
