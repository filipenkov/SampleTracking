package com.atlassian.security.auth.trustedapps.seraph.filter;

import com.atlassian.security.auth.trustedapps.filter.AuthenticationController;
import com.atlassian.seraph.filter.BaseLoginFilter;
import com.atlassian.seraph.auth.RoleMapper;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;

/**
 * Implementation of theh {@link AuthenticationController} to integrate with Atlassian Seraph.
 */
public class SeraphAuthenticationController implements AuthenticationController
{
    private final RoleMapper roleMapper;

    /**
     * @param roleMapper the configured Seraph {@link RoleMapper} for the application.
     * @throws IllegalArgumentException if the roleMapper is <code>null</code>.
     */
    public SeraphAuthenticationController(RoleMapper roleMapper)
    {
        if (roleMapper == null)
        {
            throw  new IllegalArgumentException("roleMapper must not be null!");
        }
        this.roleMapper = roleMapper;
    }

    /**
     * Checks the {@link RoleMapper} on whether or not the principal can login.
     *
     * @see AuthenticationController#canLogin(Principal, HttpServletRequest)
     */
    public boolean canLogin(Principal principal, HttpServletRequest request)
    {
        return roleMapper.canLogin(principal, request);
    }

    /**
     * Checks the request attibutes for the {@link BaseLoginFilter#OS_AUTHSTATUS_KEY}. Will return <code>true</code> if
     * the key is not present.
     */
    public boolean shouldAttemptAuthentication(HttpServletRequest request)
    {
        return request.getAttribute(BaseLoginFilter.OS_AUTHSTATUS_KEY) == null;
    }
}