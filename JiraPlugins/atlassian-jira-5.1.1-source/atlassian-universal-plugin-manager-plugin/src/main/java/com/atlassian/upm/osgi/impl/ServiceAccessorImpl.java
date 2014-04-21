package com.atlassian.upm.osgi.impl;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;
import com.atlassian.upm.osgi.ServiceAccessor;

import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.springframework.osgi.context.BundleContextAware;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.osgi.framework.Constants.SERVICE_ID;

public final class ServiceAccessorImpl implements ServiceAccessor, BundleContextAware
{
    private final PackageAccessor packageAccessor;
    private BundleContext bundleContext;

    public ServiceAccessorImpl(PackageAccessor packageAccessor)
    {
        this.packageAccessor = packageAccessor;
    }

    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = checkNotNull(bundleContext, "bundleContext");
    }

    public Iterable<Service> getServices()
    {
        try
        {
            return ServiceImpl.wrap(packageAccessor).fromArray(bundleContext.getAllServiceReferences(null, null));
        }
        catch (InvalidSyntaxException e)
        {
            // InvalidSyntaxException never thrown when filter parameter is null 
            return null;
        }
    }

    @Nullable
    public Service getService(long serviceId)
    {
        try
        {
            String filter = "(" + SERVICE_ID + "=" + serviceId + ")";
            ServiceReference[] refs = bundleContext.getAllServiceReferences(null, filter);
            if (refs == null || refs.length != 1)
            {
                return null;
            }
            return ServiceImpl.wrap(packageAccessor).fromSingleton(refs[0]);
        }
        catch (InvalidSyntaxException e)
        {
            throw new RuntimeException(e);
        }
    }
}
