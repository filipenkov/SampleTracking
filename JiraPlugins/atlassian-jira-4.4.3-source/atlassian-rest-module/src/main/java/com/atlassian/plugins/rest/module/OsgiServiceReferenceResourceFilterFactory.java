package com.atlassian.plugins.rest.module;

import com.google.common.base.Preconditions;
import com.sun.jersey.api.model.AbstractMethod;
import com.sun.jersey.spi.container.ResourceFilter;
import com.sun.jersey.spi.container.ResourceFilterFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import java.util.Collections;
import java.util.List;

/**
 * A resource filter factory that takes care of looking up the underlying {@link ResourceFilterFactory} from the given {@link ServiceReference}
 */
class OsgiServiceReferenceResourceFilterFactory implements ResourceFilterFactory
{
    private final BundleContext bundleContext;
    private final ServiceReference serviceReference;

    OsgiServiceReferenceResourceFilterFactory(final BundleContext bundleContext, final ServiceReference serviceReference)
    {
        this.bundleContext = Preconditions.checkNotNull(bundleContext);
        this.serviceReference = Preconditions.checkNotNull(serviceReference);
    }

    public List<ResourceFilter> create(final AbstractMethod am)
    {
        final ResourceFilterFactory resourceFilterFactory;
        try
        {
            resourceFilterFactory = (ResourceFilterFactory) bundleContext.getService(serviceReference);
        }
        catch (ClassCastException e)
        {
            throw new IllegalStateException("The service registered should be an instance of " + ResourceFilterFactory.class, e);
        }

        return resourceFilterFactory != null ? resourceFilterFactory.create(am) : Collections.<ResourceFilter>emptyList();
    }
}
