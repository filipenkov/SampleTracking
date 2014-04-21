package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.ConnectionPoolProperties;
import org.apache.commons.lang.math.NumberUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Form-backing bean used to set default values in the connection pool UI and convert
 * the values for storage as application attributes.
 */
public class DefaultConnectionPoolProperties implements ConnectionPoolProperties
{
    private String initialSize = ConnectionPoolPropertyConstants.DEFAULT_INITIAL_POOL_SIZE;
    private String preferredSize = ConnectionPoolPropertyConstants.DEFAULT_PREFERRED_POOL_SIZE;
    private String maximumSize = ConnectionPoolPropertyConstants.DEFAULT_MAXIMUM_POOL_SIZE;
    private String timeoutInSec = Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(ConnectionPoolPropertyConstants.DEFAULT_POOL_TIMEOUT_MS), TimeUnit.MILLISECONDS));
    private String supportedProtocol = ConnectionPoolPropertyConstants.DEFAULT_POOL_PROTOCOL;
    private String supportedAuthentication = ConnectionPoolPropertyConstants.DEFAULT_POOL_AUTHENTICATION;

    public String getInitialSize()
    {
        return initialSize;
    }

    public void setInitialSize(String initialSize)
    {
        this.initialSize = initialSize;
    }

    public String getMaximumSize()
    {
        return maximumSize;
    }

    public void setMaximumSize(String maximumSize)
    {
        this.maximumSize = maximumSize;
    }

    public String getPreferredSize()
    {
        return preferredSize;
    }

    public void setPreferredSize(String preferredSize)
    {
        this.preferredSize = preferredSize;
    }

    public String getTimeoutInSec()
    {
        return timeoutInSec;
    }

    public void setTimeoutInSec(String timeoutInSec)
    {
        this.timeoutInSec = timeoutInSec;
    }

    public String getSupportedAuthentication()
    {
        return supportedAuthentication;
    }

    public void setSupportedAuthentication(String supportedAuthentication)
    {
        this.supportedAuthentication = supportedAuthentication;
    }

    public String getSupportedProtocol()
    {
        return supportedProtocol;
    }

    public void setSupportedProtocol(String supportedProtocol)
    {
        this.supportedProtocol = supportedProtocol;
    }

    public Map<String, String> toPropertiesMap()
    {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(ConnectionPoolPropertyConstants.POOL_INITIAL_SIZE, getInitialSize());
        map.put(ConnectionPoolPropertyConstants.POOL_MAXIMUM_SIZE, getMaximumSize());
        map.put(ConnectionPoolPropertyConstants.POOL_PREFERRED_SIZE, getPreferredSize());
        map.put(ConnectionPoolPropertyConstants.POOL_TIMEOUT, Long.toString(TimeUnit.MILLISECONDS.convert(NumberUtils.toLong(getTimeoutInSec()), TimeUnit.SECONDS)));
        map.put(ConnectionPoolPropertyConstants.POOL_PROTOCOL, getSupportedProtocol());
        map.put(ConnectionPoolPropertyConstants.POOL_AUTHENTICATION, getSupportedAuthentication());

        return map;
    }

    public static ConnectionPoolProperties fromPropertiesMap(Map<String, String> map)
    {
        DefaultConnectionPoolProperties poolProperties = new DefaultConnectionPoolProperties();

        poolProperties.setInitialSize(map.get(ConnectionPoolPropertyConstants.POOL_INITIAL_SIZE));
        poolProperties.setMaximumSize(map.get(ConnectionPoolPropertyConstants.POOL_MAXIMUM_SIZE));
        poolProperties.setPreferredSize(map.get(ConnectionPoolPropertyConstants.POOL_PREFERRED_SIZE));
        poolProperties.setTimeoutInSec(Long.toString(TimeUnit.SECONDS.convert(NumberUtils.toLong(map.get(ConnectionPoolPropertyConstants.POOL_TIMEOUT)), TimeUnit.MILLISECONDS)));
        poolProperties.setSupportedProtocol(map.get(ConnectionPoolPropertyConstants.POOL_PROTOCOL));
        poolProperties.setSupportedAuthentication(map.get(ConnectionPoolPropertyConstants.POOL_AUTHENTICATION));

        return poolProperties;
    }

}
