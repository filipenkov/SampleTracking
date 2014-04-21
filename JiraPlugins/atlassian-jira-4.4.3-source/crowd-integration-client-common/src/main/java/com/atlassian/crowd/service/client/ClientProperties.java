package com.atlassian.crowd.service.client;

import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;

import java.util.Properties;

/**
 * Properties required for the Crowd Client. Normally the properties are stored in <tt>crowd.properties</tt> file.
 */
public interface ClientProperties
{
    /**
     * Updates all the properties with the new values.
     *
     * @param properties properties to update from
     */
    void updateProperties(Properties properties);

    /**
     * Returns the application name.  Used in application authentication.
     *
     * @return application name.
     */
    String getApplicationName();

    /**
     * Returns the application password used for authenticating the application.
     *
     * @return application password.
     */
    String getApplicationPassword();

    /**
     * Returns the URL of the application's authentication page.
     *
     * @return URL of the application's authentication page
     */
    String getApplicationAuthenticationURL();

    /**
     * Returns the key of the token cookie.
     *
     * @return key of the token cookie
     */
    String getCookieTokenKey();

    /**
     * Returns the key of the session cookie.
     *
     * @return key of the session cookie
     */
    String getSessionTokenKey();

    /**
     * Returns the session attribute key of the last validation date.
     *
     * @return the session attribute key of the last validation date
     */
    String getSessionLastValidation();

    /**
     * Returns how long the SSO session is valid for in minutes between session validation. If the session has not been
     * validated for the specified amount of time, it is considered expired.
     *
     * @return how long the SSO session is valid for in minutes between session validation
     */
    long getSessionValidationInterval();

    /**
     * Returns the application authentication details.
     *
     * @return application authentication details
     */
    ApplicationAuthenticationContext getApplicationAuthenticationContext();

    /**
     * Returns the HTTP proxy port number.
     *
     * @return HTTP proxy port number
     */
    String getHttpProxyPort();

    /**
     * Returns the HTTP proxy host.
     *
     * @return HTTP proxy host
     */
    String getHttpProxyHost();

    /**
     * Returns the HTTP proxy username.
     *
     * @return HTTP proxy username
     */
    String getHttpProxyUsername();

    /**
     * Returns the HTTP proxy password.
     *
     * @return HTTP proxy password
     */
    String getHttpProxyPassword();

    /**
     * Returns the maximum number of HTTP connections.
     *
     * @return maximum number of HTTP connections
     */
    String getHttpMaxConnections();

    /**
     * Returns the HTTP connection timeout value in milliseconds.
     *
     * @return HTTP connection timeout values in milliseconds
     */
    String getHttpTimeout();

    /**
     * Returns in milliseconds how long to wait without retrieving any data from the remote
     * connection.
     *
     * @return socket timeout value in milliseconds
     */
    String getSocketTimeout();

    /**
     * Returns the base URL of the client application.
     *
     * @return base URL of the client application
     */
    String getBaseURL();
}
