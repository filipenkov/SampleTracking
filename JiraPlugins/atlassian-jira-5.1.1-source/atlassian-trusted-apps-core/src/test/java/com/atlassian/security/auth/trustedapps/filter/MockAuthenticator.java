package com.atlassian.security.auth.trustedapps.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 */
public class MockAuthenticator implements Authenticator
{
    private Result result;

    public Result authenticate(HttpServletRequest request, HttpServletResponse response)
    {
        return result;
    }

    public void setResult(Result result)
    {
        this.result = result;
    }
}
