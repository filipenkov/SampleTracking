package com.atlassian.security.auth.trustedapps.seraph.filter;

import com.atlassian.security.auth.trustedapps.filter.AuthenticationListener;
import com.atlassian.security.auth.trustedapps.filter.Authenticator;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.auth.DefaultAuthenticator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Serpah implementation of {@link AuthenticationListener} that adds Seraph request and session attributes on
 * authentication sucess
 */
public class SeraphAuthenticationListener implements AuthenticationListener
{
    /**
     * Adds Seraph authentication attribute to the request and session
     */
    public void authenticationSuccess(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
    {
        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_IN_KEY, result.getUser());
        request.getSession().setAttribute(DefaultAuthenticator.LOGGED_OUT_KEY, null);
        request.setAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY, BaseLoginFilter.LOGIN_SUCCESS);
    }

    /**
     * no-op
     */
    public void authenticationFailure(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
    {
    }

    /**
     * no-op
     */
    public void authenticationError(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response)
    {
    }

    /**
     * no-op
     */
    public void authenticationNotAttempted(HttpServletRequest request, HttpServletResponse response)
    {
    }
}
