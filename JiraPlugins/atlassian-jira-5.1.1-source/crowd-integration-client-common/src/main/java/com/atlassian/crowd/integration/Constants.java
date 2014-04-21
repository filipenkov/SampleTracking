package com.atlassian.crowd.integration;

/**
 * Crowd Client constants.
 */
public class Constants
{
    /**
     * Configuration file for Crowd.
     */
    public static final String PROPERTIES_FILE = "crowd.properties";

    /**
     * System property for allowing environment variables to override properties from the properties file
     */
    public static final String USE_ENVIRONMENT_VARIABLES = "atlassian.use.environment.variables";

    /**
     * Configuration file property value for the application authentication name.
     */
    public static final String PROPERTIES_FILE_APPLICATION_NAME = "application.name";

    /**
     * Configuration file property value for the application authentication password.
     */
    public static final String PROPERTIES_FILE_APPLICATION_PASSWORD = "application.password";

    /**
     * Configuration file property value for the application authentication URL.
     */
    public static final String PROPERTIES_FILE_APPLICATION_LOGIN_URL = "application.login.url";

    /**
     * Configuration file property value for the Crowd server web services URL.
     */
    public static final String PROPERTIES_FILE_SECURITY_SERVER_URL = "crowd.server.url";

    /**
     * Configuration file property value for the Crowd server base URL.
     */
    public static final String PROPERTIES_FILE_BASE_URL = "crowd.base.url";

    /**
     * Configuration file property value for the name of Crowd SSO token cookie (optional).
     */
    public static final String PROPERTIES_FILE_COOKIE_TOKENKEY = "cookie.tokenkey";

    /**
     * Configuration file property value for the http session token key, String.
     */
    public static final String PROPERTIES_FILE_SESSIONKEY_TOKENKEY = "session.tokenkey";

    /**
     * Configuration file property value for the time in minutes between validations, 0 for every time.
     */
    public static final String PROPERTIES_FILE_SESSIONKEY_VALIDATIONINTERVAL = "session.validationinterval";

    /**
     * Configuration file property value for the http session last validation, Date.
     */
    public static final String PROPERTIES_FILE_SESSIONKEY_LASTVALIDATION = "session.lastvalidation";

    /**
     * Configuration file property value for the HTTP proxy host (optional).
     */
    public static final String PROPERTIES_FILE_HTTP_PROXY_HOST = "http.proxy.host";

    /**
     * Configuration file property value for the HTTP proxy port (optional).
     */
    public static final String PROPERTIES_FILE_HTTP_PROXY_PORT = "http.proxy.port";

    /**
     * Configuration file property value for the HTTP proxy username (optional).
     */
    public static final String PROPERTIES_FILE_HTTP_PROXY_USERNAME = "http.proxy.username";

    /**
     * Configuration file property value for the HTTP proxy password (optional).
     */
    public static final String PROPERTIES_FILE_HTTP_PROXY_PASSWORD = "http.proxy.password";

    /**
     * Configuration file property value for the maximum number of HTTP connections (optional).
     *
     * This property (if defined) will also be used as the maximum number of HTTP connections
     * per host.
     */
    public static final String PROPERTIES_FILE_HTTP_MAX_CONNECTIONS = "http.max.connections";

    /**
     * Configuration file property value for HTTP connection timeout (optional).
     */
    public static final String PROPERTIES_FILE_HTTP_TIMEOUT = "http.timeout";

    /**
     * Configuration file property value for specifying how long to wait
     * without retrieving any data from the remote connection (optional).
     */
    public static final String PROPERTIES_FILE_SOCKET_TIMEOUT = "socket.timeout";

    /**
     * The key to use when storing the http token in an Http Cookie.
     */
    public static final String COOKIE_TOKEN_KEY = "crowd.token_key";

    /**
     * Specifies the path to store cookies at.
     */
    public static final String COOKIE_PATH = "/";
    public static final String SECURITY_SERVER_NAME = "SecurityServer";
    public static final String CROWD_SERVICE_LOCATION = "services";

    public static final String CACHE_CONFIGURATION = "crowd-ehcache.xml";

    public static final String REQUEST_SSO_COOKIE_COMMITTED = "com.atlassian.crowd.integration.http.HttpAuthenticator.REQUEST_SSO_COOKIE_COMMITTED";
}