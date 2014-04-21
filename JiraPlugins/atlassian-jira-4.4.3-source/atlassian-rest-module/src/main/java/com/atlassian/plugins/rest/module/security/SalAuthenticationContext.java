package com.atlassian.plugins.rest.module.security;

import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.atlassian.plugins.rest.module.servlet.ServletUtils;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.base.Preconditions;

import java.security.Principal;

/**
 * SAL implementation of the {@link AuthenticationContext}
 * @since 1.0
 */
public class SalAuthenticationContext implements AuthenticationContext
{
    private final UserManager userManager;

    public SalAuthenticationContext(final UserManager userManager)
    {
        this.userManager = Preconditions.checkNotNull(userManager);
    }

    public Principal getPrincipal()
    {
        final String userName = getUserName();
        return userName != null ? new SalPrincipal(userName) : null;
    }

    public boolean isAuthenticated()
    {
        return getUserName() != null;
    }

    private String getUserName()
    {
        return userManager.getRemoteUsername(ServletUtils.getHttpServletRequest());
    }

    private static class SalPrincipal implements Principal
    {
        private final String userName;

        SalPrincipal(String userName)
        {
            this.userName = Preconditions.checkNotNull(userName);
        }

        public String getName()
        {
            return userName;
        }

        @Override
        public int hashCode()
        {
            return userName.hashCode();
        }

        @Override
        public boolean equals(Object obj)
        {
            return obj != null && (obj instanceof SalPrincipal) && ((SalPrincipal) obj).userName.equals(userName);
        }

        @Override
        public String toString()
        {
            return userName;
        }
    }
}
