package com.atlassian.applinks.host.spi;

import com.atlassian.applinks.api.ApplicationId;

import java.net.URI;

/**
 * Injectable component interface that is implemented by the host application
 * and contains the application specific logic to determine the application's
 * capabilities.
 *
 * @since 3.0
 */
public interface HostApplication
{
    /**
     * Returns a globally unique identifier for this server. If the host
     * application has an Atlassian server ID, it can use the server ID to generate a GUID and return this.
     * <p/>
     * To generate a GUID, based on the Atlassian server ID, use
     * e.g. new ApplicationId(UUID.nameUUIDFromBytes(getServerId().getBytes(defaultEncoding)).toString());
     * <p/>
     *
     * Otherwise the application is responsible for generating a GUID itself and storing
     * it, so that every call to this method always returns the same value.
     *
     * The returned string must use anything ONLY upper and lowercase
     * characters, the digits [0-9] and dashes (-). There are no length
     * requirements for the returned string.
     *
     * @return a globally unique identifier for this server that will never
     *         change.
     */
    ApplicationId getId();

    /**
     * @return the base URL for the application to be used when constructing links
     *         that are sent to clients, e.g. web browsers. The {@link URI} returned
     *         by this method must not have a trailing slash. e.g. "https://mydomain.com/jira"
     */
    URI getBaseUrl();
}
