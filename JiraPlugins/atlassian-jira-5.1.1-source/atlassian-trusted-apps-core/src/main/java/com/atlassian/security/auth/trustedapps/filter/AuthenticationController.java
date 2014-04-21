package com.atlassian.security.auth.trustedapps.filter;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Allows the underlying framework to communicate Trusted Apps authentication mechanim details
 */
public interface AuthenticationController
{
    /**
     * Check whether or not authentication via Trusted Apps should be tried. Tyipcally this will return
     * <code>true</code> if the current principal is not already authenticated.
     *
     * @param request the current {@link HttpServletRequest}
     * @return <code>true</code> if Trusted Apps authentication should be tried, <code>false</code> otherwise.
     */
    boolean shouldAttemptAuthentication(HttpServletRequest request);


    /**
     * Check whether the given principal can log into the application for the current request.
     *
     * @param principal the identified principal
     * @param request the current {@link HttpServletRequest}
     * @return <code>true</code> if the principal is allowed to login for the given request, <code>false</code>
     * otherwise.
     */
    boolean canLogin(Principal principal, HttpServletRequest request);
}
