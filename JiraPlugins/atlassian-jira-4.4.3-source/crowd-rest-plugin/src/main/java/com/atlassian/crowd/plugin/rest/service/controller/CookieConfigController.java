package com.atlassian.crowd.plugin.rest.service.controller;

import com.atlassian.crowd.manager.property.PropertyManager;
import com.atlassian.crowd.manager.property.PropertyManagerException;
import com.atlassian.crowd.service.client.ClientProperties;

/**
 * Controller for cookie configuration.
 */
public class CookieConfigController
{
    private final PropertyManager propertyManager;

    private final ClientProperties clientProperties;

    public CookieConfigController(final PropertyManager propertyManager, final ClientProperties clientProperties)
    {
        this.propertyManager = propertyManager;
        this.clientProperties = clientProperties;
    }

    /**
     * @return domain that the cookie should use.
     */
    public String getDomain()
    {
        try
        {
            return propertyManager.getDomain();
        }
        catch (PropertyManagerException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return <code>true</code> if "secure" flag should be set to true on the SSO cookie.
     */
    public boolean isSecureCookie()
    {
        try
        {
            return propertyManager.isSecureCookie();
        }
        catch (PropertyManagerException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return name that the cookie should use
     */
    public String getName()
    {
        return clientProperties.getCookieTokenKey();
    }
}
