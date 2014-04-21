package com.atlassian.crowd.integration.http.util;

import com.atlassian.crowd.model.authentication.CookieConfiguration;
import com.atlassian.crowd.model.authentication.UserAuthenticationContext;
import com.atlassian.crowd.service.client.ClientProperties;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Helper class for Crowd SSO token operations.
 */
public interface CrowdHttpTokenHelper
{
    /**
     * Retrieves the Crowd authentication token from the request either via:
     * <p/>
     * <ol>
     *     <li>a request attribute (<b>not</b> request parameter), OR</li>
     *     <li>a cookie on the request</li>
     * </ol>
     *
     * @param request request to look for the Crowd SSO token.
     * @param tokenName name of the request attribute and cookie for the Crowd SSO token.
     * @return value of the token if found, otherwise null.
     * @throws IllegalArgumentException if the {@code request} or {@code tokenName} is null
     */
    String getCrowdToken(HttpServletRequest request, String tokenName);

    /**
     * Removes the Crowd SSO token.  Deletes the token cookie by session the cookie in the response to max age of 0; and removes
     * the token attributes from the request.
     *
     * @param request request to invalidate the client for.
     * @param response response to invalidate the cookie for. Can be null.
     * @param clientProperties properties of the client
     * @param cookieConfig cookie configuration. Can be null if {@code response} is null since no cookie will be set.
     */
    void removeCrowdToken(HttpServletRequest request, HttpServletResponse response, ClientProperties clientProperties, CookieConfiguration cookieConfig);

    /**
     * Sets the Crowd SSO token in:
     * <ol>
     *     <li>the request: as an attribute, so the user is authenticated for the span of the request.</li>
     *     <li>the response: as a cookie, so the user is authenticated for subsequent requests.</li>
     * </ol>
     *
     * Also sets the last session validation date/time.
     *
     * @param request  request to set the attribute and session attribute for.
     * @param response response to set the cookie for. Can be null.
     * @param token    token value to use.
     * @param clientProperties properties of the client
     * @param cookieConfig Cookie configuration
     */
    void setCrowdToken(HttpServletRequest request, HttpServletResponse response, String token, ClientProperties clientProperties, CookieConfiguration cookieConfig);

    /**
     * Returns the user authentication context from a request.
     *
     * @param request HttpRequest object
     * @param username user's name
     * @param password user's password
     * @param clientProperties properties of the client
     * @return UserAuthenticationContext.
     */
    UserAuthenticationContext getUserAuthenticationContext(HttpServletRequest request, String username, String password, ClientProperties clientProperties);

    /**
     * Returns the ValidationFactor extractor.
     *
     * @return validation factor extractor
     */
    CrowdHttpValidationFactorExtractor getValidationFactorExtractor();
}
