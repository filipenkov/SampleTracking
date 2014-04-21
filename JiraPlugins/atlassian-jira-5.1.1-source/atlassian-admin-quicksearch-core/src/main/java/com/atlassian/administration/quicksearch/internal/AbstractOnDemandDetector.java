package com.atlassian.administration.quicksearch.internal;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

/**
 * {@link com.atlassian.administration.quicksearch.internal.OnDemandDetector} that
 * uses {@link org.osgi.framework.BundleContext} to get component instance.
 *
 * @since v4.4
 */
public abstract class AbstractOnDemandDetector implements OnDemandDetector, BundleContextAware
{

    private volatile BundleContext bundleContext;

    @Override
    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }

    @Override
    public final Object getOnDemandComponent(String componentClass)
    {
        if (!isOnDemandMode())
        {
            throw new IllegalStateException("Not in OnDemand environment");
        }
        return getComponentNoCheck(componentClass);
    }

    protected final Object getComponentNoCheck(String componentClass)
    {
        final ServiceReference serviceRef = bundleContext.getServiceReference(componentClass);
        return bundleContext.getService(serviceRef);
    }
}
