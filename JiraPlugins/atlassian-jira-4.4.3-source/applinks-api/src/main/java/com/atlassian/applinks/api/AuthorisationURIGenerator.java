package com.atlassian.applinks.api;

import com.atlassian.applinks.api.auth.AuthenticationProvider;

import java.net.URI;

/**
 * Used to generate URIs for users to supply credentials to {@link AuthenticationProvider}s. 
 *
 * @since 3.0
 */
public interface AuthorisationURIGenerator
{
    /**
     * <p>
     * The URI that can be used to configure authentication for the requested
     * resource.
     * </p>
     * <p>
     * The typical scenario is a call to an OAuth-protected remote resource for
     * which the caller does not have an access token. If the caller has the
     * ability to send a redirect (in case of a plugin servlet or webwork
     * action), it would do so using this URL. This URL will take the user to a
     * local endpoint that will perform the "OAuth dance":
     * <li>request a Request Token from the remote OAuth provider</li>
     * <li>redirect the client to the provider's authorize URL, using itself
     * for the callback</li>
     * <li>on successful approval by the user, swap the request token for an
     * access token</li>
     * <li>redirect the user back to original resource (the plugin's servlet
     * or action)</li>
     * </p>
     * <p>
     * If the caller does not have the ability to perform an HTTP redirect to
     * this URL (possibly because it's a Web Panel), it can display a link or
     * button that will open the URL in a popup dialog with an iframe, allowing
     * the user to perform the oauth dance at a later time.
     * </p>
     *
     * @param callback the URI to redirect to after authentication is complete
     * @return  the URL that can be used to provide authentication for the
     * requested resource, or null if this request factory doesn't support
     * authorisation via user intervention. Note, if this method returns a
     * non-null value, {@link #getAuthorisationURI()} <strong>MUST</strong>
     * also return a non-null value.
     */
    URI getAuthorisationURI(URI callback);

    /**
     * Provides a URI that can be used to configure authentication for the
     * requested resource. This URI will not result in a callback.
     *
     * ApplicationLinkRequestFactory implementations that support authorisation
     * URIs must return non-null values for this method at a minimum. Support for
     * {@link #getAuthorisationURI(URI callback)} is optional.
     *
     * @return the URL that can be used to configure authentication for the
     * requested resource, or null if this request factory doesn't support
     * authorisation via user intervention. If {@link #getAuthorisationURI(URI)}
     * returns a non-null value, this method <strong>MUST</strong> also return
     * a non-null value.
     * @see #getAuthorisationURI(URI)
     */
    URI getAuthorisationURI();
}
