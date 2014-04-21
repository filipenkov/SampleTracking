package com.atlassian.crowd.integration.http;

import com.atlassian.crowd.exception.*;
import com.atlassian.crowd.model.user.User;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This interface is used to manage HTTP authentication.
 *
 * It is the fundamental class for web/SSO authentication integration.
 *
 * This interface contains many convenience methods for authentication integration with existing applications.
 * For most applications, using the following methods will be sufficient to achieve SSO:
 * <ol>
 * <li><code>authenticate:</code> authenticate a user.</li>
 * <li><code>isAuthenticated:</code> determine if a request is authenticated.</li>
 * <li><code>getUser:</code> retrieve the user for an authenticated request.</li>
 * <li><code>logout:</code> sign the user out.</li>
 * </ol>
 *
 * Use the <code>HttpAuthenticatorFactory<code> to get an
 * instance of a class, or use an IoC container (like Spring)
 * to manage the underlying implementation as a singleton.
 */
public interface CrowdHttpAuthenticator
{
    /**
     * Attempts to retrieve the currently authenticated User from the request.
     *
     * This will attempt to find the Crowd SSO token via:
     * <ol>
     *     <li>a request attribute (<b>not</b> the request parameter), OR</li>
     *     <li>a cookie on the request</li>
     * </ol>
     *
     * @param request HTTP request, possibly containing a Crowd SSO cookie.
     * @return authenticated {@code User} or {@code null} if the there is no authenticated user.
     * @throws com.atlassian.crowd.exception.InvalidTokenException          if the token in the request is not valid.
     * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application and password are not valid.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason.
     */
    User getUser(HttpServletRequest request) throws InvalidTokenException, ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Authenticates the user based on provided credentials.
     * <p/>
     * Validation factors (such as IP address) are extracted from the request.
     * <p/>
     * If the user is successfully authenticated, the Crowd SSO token is placed in:
     * <ol>
     *     <li>the request: as an attribute, so the user is authenticated for the span of the request.</li>
     *     <li>the response: as a cookie, so the user is authenticated for subsequent requests.</li>
     * </ol>
     * <p/>
     * If the credentials fail authentication, any existing Crowd SSO token is removed from:
     * <ol>
     *     <li>the request attribute.</li>
     *     <li>the response as a cookie as a cookie with a max-age of 0.</li>
     * </ol>
     *
     * @param request request to set the Crowd SSO token
     * @param response response to set the Crowd SSO token cookie
     * @param username username to authenticate
     * @param password password of the user
     * @return the authenticated user if the authentication was successful, otherwise an exception is thrown.
     * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application and password are not valid.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason.
     */
    User authenticate(HttpServletRequest request, HttpServletResponse response, String username, String password)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, ApplicationAccessDeniedException, ExpiredCredentialException, InactiveAccountException, InvalidTokenException;

    /**
     * Authenticates the user without validating password.
     * <p/>
     * Validation factors (such as IP address) are extracted from the request.
     * <p/>
     * If the user is successfully authenticated, the Crowd SSO token is placed in:
     * <ol>
     *     <li>the request: as an attribute, so the user is authenticated for the span of the request.</li>
     *     <li>the response: as a cookie, so the user is authenticated for subsequent requests.</li>
     * </ol>
     * <p/>
     * If authentication fails, any existing Crowd SSO token is removed from:
     * <ol>
     *     <li>the request attribute.</li>
     *     <li>the response as a cookie as a cookie with a max-age of 0.</li>
     * </ol>
     *
     * @param request request to set the Crowd SSO token
     * @param response response to set the Crowd SSO token cookie
     * @param username username to authenticate
     * @return the authenticated user if the authentication was successful, otherwise an exception is thrown.
     * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application and password are not valid.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason.
     */
    User authenticateWithoutValidatingPassword(HttpServletRequest request, HttpServletResponse response, String username)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException, ApplicationAccessDeniedException, InactiveAccountException, InvalidTokenException;

    /**
     * Tests whether a request is authenticated via SSO.
     *
     * This only tests against the Crowd server if the validation interval is exceeded, this value is obtained from
     * crowd.properties <b>AND</b> that there is a valid token present for the user in the Crowd Cookie.
     *
     * The last validated date/time attribute of the request session is updated. 
     *
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return <code>true</code> if and only if the request has been authenticated.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason.
     */
    boolean isAuthenticated(HttpServletRequest request, HttpServletResponse response) throws OperationFailedException;

    /**
     * Logs out the authenticated user.
     *
     * Removes the cookie from the response and request attribute.  Invalidates the token on the server.
     *
     * @param request request contains the Crowd SSO token to invalidate and hence log the user out.
     * @param response response returns a request to remove the token cookie from the user browser.
     * @throws com.atlassian.crowd.exception.ApplicationPermissionException if the application is not permitted to perform the requested operation on the server.
     * @throws com.atlassian.crowd.exception.InvalidAuthenticationException if the application and password are not valid.
     * @throws com.atlassian.crowd.exception.OperationFailedException       if the operation has failed for an unknown reason.
     */
    void logout(HttpServletRequest request, HttpServletResponse response)
            throws ApplicationPermissionException, InvalidAuthenticationException, OperationFailedException;

    /**
     * Retrieves the Crowd authentication token from the request.
     *
     * @param request request to look for the Crowd SSO token.
     * @return value of the token if found, otherwise null.
     * @throws IllegalArgumentException if the {@code request} is null
     */
    String getToken(HttpServletRequest request);
}

