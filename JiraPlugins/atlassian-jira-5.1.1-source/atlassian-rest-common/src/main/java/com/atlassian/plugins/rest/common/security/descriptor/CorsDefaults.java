package com.atlassian.plugins.rest.common.security.descriptor;

import java.util.Set;

/**
 * Defaults to apply for Cross-Origin Resource Sharing when the
 * {@link com.atlassian.plugins.rest.common.security.AllowCors}
 * annotation is used.
 *
 * @since 2.6
 */
public interface CorsDefaults
{
    /**
     * Given an origin which has already passed an {@link #allowsOrigin(String)} check, tests that origin to determine
     * if it is whitelisted for making a <i>credentialed</i> CORS request, on behalf of a specific user.
     *
     * @param uri The origin that has already been allowed.  Will never be null
     * @return True if the origin allows origin requests that contain credentials such as cookies or HTTP auth
     * @throws IllegalArgumentException Thrown if the uri is not a valid origin or is null.
     */
    boolean allowsCredentials(String uri) throws IllegalArgumentException;

    /**
     * Tests the provided origin to determine if it is whitelisted for making <i>non-credentialed</i> CORS requests.
     * 
     * @param uri The origin.  Will never be null
     * @return True if the origin provided matches any values in the whitelist
     * @throws IllegalArgumentException Thrown if the uri is not a valid URL or is null.
     */
    boolean allowsOrigin(String uri) throws IllegalArgumentException;

    /**
     * For the provided origin, returns a set of HTTP headers which the browser may include when making a request.
     * These headers are only relevant for a CORS preflight check, which is made for non-simple HTTP requests.
     *
     * @param uri the origin that has already been allowed.  Will never be null
     * @return set of allowed non-simple (see spec) HTTP headers.  Must not be null
     * @throws IllegalArgumentException Thrown if the uri is not a valid origin or is null.
     * @see com.atlassian.plugins.rest.common.security.CorsHeaders#ACCESS_CONTROL_ALLOW_HEADERS
     */
    Set<String> getAllowedRequestHeaders(String uri) throws IllegalArgumentException;

    /**
     * For the provided origin, returns a set of HTTP headers which the browser's CORS support can forward on to the
     * underlying request. For resources accessed via CORS which have non-simple headers they return, only those
     * headers which are allowed this set will actually be exposed by the browser after the request completes. These
     * headers are only relevant for simple HTTP requests, which do not require a CORS preflight check.
     *
     * @param uri the origin that has already been allowed.  Will never be null
     * @return set of allowed simple (see spec) HTTP headers.  Must not be null
     * @throws IllegalArgumentException Thrown if the uri is not a valid origin or is null.
     * @see com.atlassian.plugins.rest.common.security.CorsHeaders#ACCESS_CONTROL_EXPOSE_HEADERS
     */
    Set<String> getAllowedResponseHeaders(String uri) throws IllegalArgumentException;
}
