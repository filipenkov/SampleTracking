package com.atlassian.plugins.rest.common.sal.websudo;

import static com.google.common.base.Preconditions.checkNotNull;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import javax.ws.rs.ext.Provider;
import java.util.Collections;
import java.util.List;

/**
 * <p>A {@link com.sun.jersey.spi.container.ResourceFilterFactory} that checks wether the client is authenticated or not.<p>
 * @see WebSudoResourceFilter
 */
@Provider
public class WebSudoResourceFilterFactory implements ResourceFilterFactory
{
    private final WebSudoResourceContext authenticationContext;

    public WebSudoResourceFilterFactory(final WebSudoResourceContext authenticationContext)
    {
        this.authenticationContext = checkNotNull(authenticationContext);
    }

    public List<ResourceFilter> create(final AbstractMethod abstractMethod)
    {
        return Collections.<ResourceFilter>singletonList(new WebSudoResourceFilter(abstractMethod, authenticationContext));
    }
}