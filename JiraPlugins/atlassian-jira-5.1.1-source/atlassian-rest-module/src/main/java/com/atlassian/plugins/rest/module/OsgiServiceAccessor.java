package com.atlassian.plugins.rest.module;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import javax.annotation.Nullable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

class OsgiServiceAccessor<S>
{
    private static final String FILTER = "(plugin=com.atlassian.plugins.rest)";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Class<S> serviceType;
    private final BundleContext bundleContext;

    private ServiceReference[] references;
    private final OsgiFactory<? extends S> factory;

    OsgiServiceAccessor(final Class<S> serviceType, final BundleContext bundleContext, final OsgiFactory<? extends S> factory)
    {
        this.serviceType = Preconditions.checkNotNull(serviceType);
        this.bundleContext = Preconditions.checkNotNull(bundleContext);
        this.factory = Preconditions.checkNotNull(factory);
    }

    Collection<? extends S> get()
    {
        try
        {
            references = bundleContext.getServiceReferences(serviceType.getName(), FILTER);
            if (references == null) // no service found
            {
                return Collections.emptySet();
            }
            else
            {
                return Lists.transform(Arrays.asList(references), new Function<ServiceReference, S>()
                {
                    public S apply(@Nullable ServiceReference serviceReference)
                    {
                        return factory.getInstance(bundleContext, serviceReference);
                    }
                });
            }
        }
        catch (InvalidSyntaxException e)
        {
            logger.error("Could not get service references", e);
            return Collections.emptyList();
        }
    }

    void release()
    {
        if (references != null)
        {
            for (ServiceReference reference : references)
            {
                bundleContext.ungetService(reference);
            }
        }
    }
}
