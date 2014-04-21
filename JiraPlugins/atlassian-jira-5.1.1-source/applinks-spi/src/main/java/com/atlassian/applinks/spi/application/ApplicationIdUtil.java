package com.atlassian.applinks.spi.application;

import com.atlassian.applinks.api.ApplicationId;

import java.net.URI;
import java.util.UUID;

/**
 * @since 3.0
 */
public class ApplicationIdUtil
{
    /**
     * Generates a consistent, unique {@link ApplicationId} for the specified baseUri. Subsequent calls to this method
     * with the equivalent baseUri will return the same result. 'Equivalent' baseUris are URIs that are
     * lexicographically equal after:
     * <ul>
     *  <li>trailing slashes are removed; and</li>
     *  <li>any Non-ASCII characters are encoded (see {@link URI#toASCIIString()}); and</li>
     *  <li>normalization (see {@link URI#normalize()}).</li>
     * </ul>
     *
     * @param baseUri the base URI of a remote application
     * @return a consistent, unique {@link ApplicationId} for the supplied {@link URI}
     *
     * @since 3.0
     */
    public static ApplicationId generate(final URI baseUri)
    {
        String normalisedUri = baseUri.normalize().toASCIIString();
        while (normalisedUri.endsWith("/") && normalisedUri.length() > 1)
        {
            normalisedUri = normalisedUri.substring(0, normalisedUri.length() - 1);
        }
        final String idString = UUID.nameUUIDFromBytes(normalisedUri.getBytes()).toString();
        return new ApplicationId(idString);
    }

}
