package com.atlassian.plugins.rest.common.security.jersey;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import com.atlassian.plugins.rest.common.security.AuthenticationContext;

import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;

/**
 * <p>A {@link ResourceFilterFactory} that checks wether the client is authenticated or not.<p>
 * @see AuthenticatedResourceFilter
 */
@Provider
public class AuthenticatedResourceFilterFactory implements ResourceFilterFactory
{
    private final AuthenticationContext authenticationContext;

    public AuthenticatedResourceFilterFactory(final AuthenticationContext authenticationContext)
    {
        this.authenticationContext = Preconditions.checkNotNull(authenticationContext);
    }

    public List<ResourceFilter> create(AbstractMethod abstractMethod)
    {
        return Collections.<ResourceFilter>singletonList(new AuthenticatedResourceFilter(abstractMethod, authenticationContext));
    }
}
