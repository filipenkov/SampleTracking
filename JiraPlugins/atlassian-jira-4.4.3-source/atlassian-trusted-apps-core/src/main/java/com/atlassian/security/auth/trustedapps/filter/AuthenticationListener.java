package com.atlassian.security.auth.trustedapps.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Allows the underlying framework to take some actions on authentication events.
 */
public interface AuthenticationListener
{
    void authenticationSuccess(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response);

    void authenticationFailure(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response);

    void authenticationError(Authenticator.Result result, HttpServletRequest request, HttpServletResponse response);

    void authenticationNotAttempted(HttpServletRequest request, HttpServletResponse response);
}
