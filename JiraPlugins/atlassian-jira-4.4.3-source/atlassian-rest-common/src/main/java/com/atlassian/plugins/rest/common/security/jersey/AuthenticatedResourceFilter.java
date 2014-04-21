package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.atlassian.plugins.rest.common.security.AuthenticationContext;
import com.atlassian.plugins.rest.common.security.AuthenticationRequiredException;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.ContainerResponseFilter;
import com.sun.jersey.spi.container.ResourceFilter;

/**
 * <p>This is a Jersey resource filter that checks wether the current client is authenticated or not.
 * If the client is not authenticated then an {@link AuthenticationRequiredException} is thrown.</p>
 * <p>Resources can be marked as not needing authentication by using the {@link AnonymousAllowed} annotation.</p>
 */
class AuthenticatedResourceFilter implements ResourceFilter, ContainerRequestFilter
{
    private final AbstractMethod abstractMethod;
    private final AuthenticationContext authenticationContext;

    public AuthenticatedResourceFilter(AbstractMethod abstractMethod, AuthenticationContext authenticationContext)
    {
        this.abstractMethod = Preconditions.checkNotNull(abstractMethod);
        this.authenticationContext = Preconditions.checkNotNull(authenticationContext);
    }

    public ContainerRequestFilter getRequestFilter()
    {
        return this;
    }

    public ContainerResponseFilter getResponseFilter()
    {
        return null;
    }

    public ContainerRequest filter(ContainerRequest request)
    {
        if (!isAnonymousAllowed() && !isClientAuthenticated())
        {
            throw new AuthenticationRequiredException();
        }
        return request;
    }

    private boolean isAnonymousAllowed()
    {
        return (abstractMethod.getMethod() != null && abstractMethod.getMethod().getAnnotation(AnonymousAllowed.class) != null)
                || abstractMethod.getResource().getAnnotation(AnonymousAllowed.class) != null;
    }

    private boolean isClientAuthenticated()
    {
        return authenticationContext.isAuthenticated();
    }
}
