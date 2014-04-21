package com.atlassian.security.auth.trustedapps.request.commonshttpclient;

import com.atlassian.security.auth.trustedapps.request.TrustedRequest;

import org.apache.commons.httpclient.HttpMethod;

import com.mockobjects.dynamic.C;
import com.mockobjects.dynamic.Mock;

import junit.framework.TestCase;

/**
 * Testing {@link CommonsHttpClientTrustedRequest}
 */
public class TestCommonsHttpClientTrustedRequest extends TestCase
{
    private Mock mockHttpMethod;

    private TrustedRequest trustedRequest;

    protected void setUp() throws Exception
    {
        mockHttpMethod = new Mock(HttpMethod.class);
        trustedRequest = new CommonsHttpClientTrustedRequest((HttpMethod) mockHttpMethod.proxy());
    }

    protected void tearDown() throws Exception
    {
        trustedRequest = null;
    }

    public void testCannotCreateWithNullHttpMethod()
    {
        try
        {
            new CommonsHttpClientTrustedRequest(null);
            fail();
        }
        catch (IllegalArgumentException e)
        {
            // ignored
        }
    }

    public void testAddRequestParameter()
    {
        final String paramName = "some name";
        final String paramValue = "some value";
        mockHttpMethod.expect("addRequestHeader", C.args(C.eq(paramName), C.eq(paramValue)));

        trustedRequest.addRequestParameter(paramName, paramValue);
    }
}
