package com.atlassian.security.auth.trustedapps.request.commonshttpclient;

import com.atlassian.security.auth.trustedapps.request.TrustedRequest;
import org.apache.commons.httpclient.HttpMethod;

/**
 * Commons Http Client implementation of {@link TrustedRequest}
 */
public class CommonsHttpClientTrustedRequest implements TrustedRequest
{
    private final HttpMethod httpMethod;

    public CommonsHttpClientTrustedRequest(final HttpMethod httpMethod)
    {
        if (httpMethod == null)
        {
            throw new IllegalArgumentException("HttpMethod must not be null!");
        }
        this.httpMethod = httpMethod;
    }

    public void addRequestParameter(String name, String value)
    {
        httpMethod.addRequestHeader(name, value);
    }
}
