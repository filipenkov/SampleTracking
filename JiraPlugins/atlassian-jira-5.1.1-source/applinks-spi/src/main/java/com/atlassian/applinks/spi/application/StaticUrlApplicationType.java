package com.atlassian.applinks.spi.application;

import java.net.URI;

import com.atlassian.applinks.api.ApplicationType;

/**
 * Application types can implement this interface if there is only ever one URL that they will point at.  This is useful
 * for online services.
 *
 * @since 3.5
 */
public interface StaticUrlApplicationType extends ApplicationType, IdentifiableType
{
    /**
     * Get the URL that this application type lives at
     *
     * @return The URL
     */
    URI getStaticUrl();
}
