package com.atlassian.applinks.api;

import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.sal.api.net.Request;
import com.atlassian.sal.api.net.Response;
import com.atlassian.sal.api.net.ResponseException;

/**
 * Provides authenticated {@link ApplicationLinkRequest} objects. Use the
 * {@link ApplicationLink#createAuthenticatedRequestFactory()} or
 * {@link ApplicationLink#createAuthenticatedRequestFactory(Class)} method to create an
 * {@link ApplicationLinkRequestFactory} targeting a particular remote {@link ApplicationLink}.
 *
 * @since 3.0
 */
public interface ApplicationLinkRequestFactory extends AuthorisationURIGenerator
{
    /**
     * Creates a request of the given method type to the given url. ApplicationLinkRequestFactory implementations can
     * be <em>transparently authenticated</em> by different {@link AuthenticationProvider}s. See 
     * {@link ApplicationLink#createAuthenticatedRequestFactory()} for more details.
     * <br /><br />
     * Example usage:
     *
     * <blockquote><pre>
     *  {@link ApplicationLinkRequestFactory} requestFactory = {@link ApplicationLink#createAuthenticatedRequestFactory()};
     *  {@link ApplicationLinkRequest} request;
     *
     *  try {
     *      request = requestFactory.createRequest({@link Request.MethodType#GET}, "/rest/my-plugin/1.0/my-resource");
     *  } catch ({@link CredentialsRequiredException} e) {
     *      // we don't have credentials stored for the context user, so prompt user to authenticate
     *      // using the URI provided by {@link CredentialsRequiredException#getAuthorisationURI()}
     *      // ...
     *  }
     *
     *  try {
     *      boolean success = request.execute(new {@link ApplicationLinkResponseHandler}&lt;Boolean&gt;() {
     *
     *          Boolean handle({@link Response} response) throws {@link ResponseException} {
     *              return response.{@link Response#isSuccessful})
     *          }
     *
     *          Boolean credentialsRequired({@link Response} response) throws {@link ResponseException} {
     *              // the remote server rejected our credentials, so prompt the user to authenticate
     *              // using the URI provided by {@link ApplicationLinkRequestFactory#getAuthorisationURI()}
     *              // ...
     *          }
     *
     *      });
     *
     *  } catch ({@link ResponseException} re) {
     *      // the request failed to complete normally
     *  }
     * </pre></blockquote>
     *
     * @param methodType The HTTP method type
     * @param url The target of the request. If you specify a URI that <em>does not</em> start with a protocol string
     * (e.g. http: or https:) the target {@link ApplicationLink}'s rpcUrl will be pre-pended to the request
     * URI. Specified absolute URIs will be used as is.
     *
     * @return The {@link ApplicationLinkRequest} object
     * @throws CredentialsRequiredException when the target {@link ApplicationLink} requires authentication, but no
     * credentials are available for the context user.
     *
     * @see CredentialsRequiredException
     */
    ApplicationLinkRequest createRequest(Request.MethodType methodType, String url) throws CredentialsRequiredException;
}
