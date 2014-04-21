package com.atlassian.plugins.rest.common.security.jersey;

import com.atlassian.plugins.rest.common.security.RequiresXsrfCheck;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;

import java.util.Collections;
import java.util.List;
import javax.ws.rs.ext.Provider;

/**
 * Factory for the XSRF resource filter
 *
 * @since 2.4
 */
@Provider
public class XsrfResourceFilterFactory implements ResourceFilterFactory
{
    public List<ResourceFilter> create(final AbstractMethod method)
    {
        if (method.isAnnotationPresent(RequiresXsrfCheck.class)
                || method.getResource().isAnnotationPresent(RequiresXsrfCheck.class))
        {
            return Collections.<ResourceFilter>singletonList(new XsrfResourceFilter());
        }
        return Collections.emptyList();
    }
}
