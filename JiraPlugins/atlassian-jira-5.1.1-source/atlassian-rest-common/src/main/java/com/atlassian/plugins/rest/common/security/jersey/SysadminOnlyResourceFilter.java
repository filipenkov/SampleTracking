package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.AuthorisationException;
import com.atlassian.plugins.rest.common.security.AuthenticationRequiredException;
import com.atlassian.sal.api.user.UserManager;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ContainerRequest;
import com.google.common.base.Preconditions;

import javax.ws.rs.ext.Provider;

/**
 * Filter that can be used to restrict access to resources to system administrators.
 *
 * @since 1.1
 */
@Provider
public class SysadminOnlyResourceFilter implements ResourceFilter
{
    private final UserManager userManager;

    public SysadminOnlyResourceFilter(UserManager userManager)
    {
        this.userManager = Preconditions.checkNotNull(userManager);
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return new ContainerRequestFilter()
        {
            public ContainerRequest filter(ContainerRequest containerRequest)
            {
                String username = userManager.getRemoteUsername();
                if (username == null)
                {
                    throw new AuthenticationRequiredException();
                }
                if (!userManager.isSystemAdmin(username))
                {
                    throw new AuthorisationException();
                }
                return containerRequest;
            }
        };
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }
}

