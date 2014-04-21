package com.atlassian.security.auth.trustedapps.request;

/**
 * This represent a request made with trusted
 */
public interface TrustedRequest
{
    void addRequestParameter(String name, String value);
}
