package com.atlassian.applinks.spi.auth;

import com.atlassian.applinks.api.ApplicationLink;

/**
 * This interface can be used by AuthentiationProviderPluginModules that can also configure inbound authentication.
 *
 * @since v3.2.1
 */
public interface IncomingTrustAuthenticationProviderPluginModule
{
    /**
     * Return true if this authentication type can be used by the application link to make request to this application.
     *
     * @param applicationLink the application to the remote application, which wants to make requests to this application.
     *
     * @return true if the remote application can make requests to this application.
     */
    boolean incomingEnabled(ApplicationLink applicationLink);
}
