package com.atlassian.crowd.service.client;

import com.atlassian.crowd.model.authentication.ApplicationAuthenticationContext;

public abstract class AbstractClientProperties implements ClientProperties
{
    // members
    protected String applicationName = null;
    protected String applicationPassword = null;
    protected String applicationAuthenticationURL = null;
    protected String cookieTokenKey = null;
    protected String sessionTokenKey = null;
    protected String sessionLastValidation = null;
    protected long sessionValidationInterval = 0;
    protected String baseURL = null;

    protected String httpProxyPort = null;
    protected String httpProxyHost = null;
    protected String httpProxyUsername = null;
    protected String httpProxyPassword = null;

    protected String httpMaxConnections = null;
    protected String httpTimeout = null;
    protected String socketTimeout = null;

    protected ApplicationAuthenticationContext applicationAuthenticationContext = null;

    public String getApplicationName()
    {
        return applicationName;
    }

    public String getApplicationPassword()
    {
        return applicationPassword;
    }

    public String getApplicationAuthenticationURL()
    {
        return applicationAuthenticationURL;
    }

    public String getCookieTokenKey()
    {
        return cookieTokenKey;
    }

    public String getSessionTokenKey()
    {
        return sessionTokenKey;
    }

    public String getSessionLastValidation()
    {
        return sessionLastValidation;
    }

    public long getSessionValidationInterval()
    {
        return sessionValidationInterval;
    }

    public ApplicationAuthenticationContext getApplicationAuthenticationContext()
    {
        return applicationAuthenticationContext;
    }

    public String getHttpProxyPort()
    {
        return httpProxyPort;
    }

    public String getHttpProxyHost()
    {
        return httpProxyHost;
    }

    public String getHttpProxyUsername()
    {
        return httpProxyUsername;
    }

    public String getHttpProxyPassword()
    {
        return httpProxyPassword;
    }

    public String getHttpMaxConnections()
    {
        return httpMaxConnections;
    }

    public String getHttpTimeout()
    {
        return httpTimeout;
    }

    public String getSocketTimeout()
    {
        return socketTimeout;
    }

    public String getBaseURL()
    {
        return baseURL;
    }

    @Override
    public String toString()
    {
        final StringBuilder sb = new StringBuilder();
        sb.append("ClientPropertiesGeneric");
        sb.append("{applicationName='").append(applicationName).append('\'');
        sb.append(", applicationPassword='").append(applicationPassword).append('\'');
        sb.append(", applicationAuthenticationURL='").append(applicationAuthenticationURL).append('\'');
        sb.append(", cookieTokenKey='").append(cookieTokenKey).append('\'');
        sb.append(", sessionTokenKey='").append(sessionTokenKey).append('\'');
        sb.append(", sessionLastValidation='").append(sessionLastValidation).append('\'');
        sb.append(", sessionValidationInterval=").append(sessionValidationInterval);
        sb.append(", baseURL='").append(baseURL).append('\'');
        sb.append(", httpProxyPort='").append(httpProxyPort).append('\'');
        sb.append(", httpProxyHost='").append(httpProxyHost).append('\'');
        sb.append(", httpProxyUsername='").append(httpProxyUsername).append('\'');
        sb.append(", httpProxyPassword='").append(httpProxyPassword).append('\'');
        sb.append(", httpMaxConnections='").append(httpMaxConnections).append('\'');
        sb.append(", httpTimeout='").append(httpTimeout).append('\'');
        sb.append(", socketTimeout='").append(socketTimeout).append('\'');
        sb.append(", applicationAuthenticationContext=").append(applicationAuthenticationContext);
        sb.append('}');
        return sb.toString();
    }

}