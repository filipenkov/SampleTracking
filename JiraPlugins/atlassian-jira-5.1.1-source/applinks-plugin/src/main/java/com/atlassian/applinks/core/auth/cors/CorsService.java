package com.atlassian.applinks.core.auth.cors;

import com.atlassian.applinks.api.ApplicationLink;

import java.net.URI;
import java.util.Collection;

/**
 * @since 3.7
 */
public interface CorsService
{
    /**
     * Interrogates the provided {@link ApplicationLink} to determine whether it allows CORS requests with credentials.
     * 
     * @param link the link to check
     * @return {@code true} if credentialed requests have been enabled for the link; otherwise, {@code false}
     */
    boolean allowsCredentials(ApplicationLink link);

    /**
     * Disables CORS requests with credentials for the provided {@link ApplicationLink}.
     * 
     * @param link the link for which credentialed requests should be disabled
     */
    void disableCredentials(ApplicationLink link);

    /**
     * Enables CORS requests with credentials for the provided {@link ApplicationLink}.
     *
     * @param link the link for which credentialed requests should be enabled
     */
    void enableCredentials(ApplicationLink link);

    /**
     * Searches across all registered {@link ApplicationLink}s and returns any which match the provided origin.
     * <p/>
     * Note: The origin URL for CORS only includes scheme, host and port information. Further context is not
     * included. As a result, if multiple Application Links are registered for the same scheme, host and port
     * with different context paths, multiple links will be returned. This is a limitation of the way origins
     * are expressed in the CORS specification.
     *
     * @param origin the URL of the CORS origin
     * @return 0 or more links which match the scheme, host and port of the provided origin
     */
    Collection<ApplicationLink> getApplicationLinksByOrigin(String origin);

    /**
     * Searches across all registered {@link ApplicationLink}s and returns any which match the provided URI. In
     * addition to finding links using a URI-parsed origin, this method can also be used to find links sharing
     * the same scheme, host and port as another application link.
     * <p/>
     * Note: When using this method to search with an application link's URI, note that that application link will
     * be included in the results.
     *
     * @param uri the URI to search by
     * @return 0 or more links which match the scheme, host and port of the provided URI
     */
    Collection<ApplicationLink> getApplicationLinksByUri(URI uri);

    /**
     * Attempts to {@link #getApplicationLinksByOrigin(String)} get} {@link ApplicationLink}(s) with the provided
     * origin and, if at least one is not found, throws an {@link IllegalArgumentException}.
     *
     * @param origin the URL of the CORS origin
     * @return at least one link matching the provided URL
     * @throws IllegalArgumentException Thrown if no links match the provided URL
     */
    Collection<ApplicationLink> getRequiredApplicationLinksByOrigin(String origin) throws IllegalArgumentException;
}
