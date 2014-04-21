package com.atlassian.applinks.api;

import java.net.URI;

/**
 * <p>
 * Thrown by {@link ApplicationLinkRequestFactory#createRequest} when the endpoint requires authentication, but no
 * credentials are available. Consumers should redirect the user to the appropriate authorisation URI to allow the user
 * to authenticate with the remote application.
 * </p>
 *
 * @see AuthorisationURIGenerator#getAuthorisationURI()
 * @since 3.0
 */
public class CredentialsRequiredException extends Exception implements AuthorisationURIGenerator
{
    private final AuthorisationURIGenerator authorisationURIGenerator;

    public CredentialsRequiredException(final AuthorisationURIGenerator authorisationURIGenerator, final String message)
    {
        super(message);
        this.authorisationURIGenerator = authorisationURIGenerator;
    }

    public URI getAuthorisationURI()
    {
        return authorisationURIGenerator.getAuthorisationURI();
    }

    public URI getAuthorisationURI(final URI callback)
    {
        return authorisationURIGenerator.getAuthorisationURI(callback);
    }
}
