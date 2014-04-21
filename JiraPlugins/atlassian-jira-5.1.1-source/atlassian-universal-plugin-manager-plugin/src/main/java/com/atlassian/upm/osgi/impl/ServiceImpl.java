package com.atlassian.upm.osgi.impl;

import javax.annotation.Nullable;

import com.atlassian.upm.osgi.Bundle;
import com.atlassian.upm.osgi.PackageAccessor;
import com.atlassian.upm.osgi.Service;

import com.google.common.collect.ImmutableList;

import org.apache.sling.commons.osgi.OsgiUtil;
import org.osgi.framework.ServiceReference;

import static java.util.Arrays.asList;
import static org.apache.sling.commons.osgi.OsgiUtil.toInteger;
import static org.apache.sling.commons.osgi.OsgiUtil.toLong;
import static org.apache.sling.commons.osgi.OsgiUtil.toStringArray;
import static org.osgi.framework.Constants.OBJECTCLASS;
import static org.osgi.framework.Constants.SERVICE_DESCRIPTION;
import static org.osgi.framework.Constants.SERVICE_ID;
import static org.osgi.framework.Constants.SERVICE_PID;
import static org.osgi.framework.Constants.SERVICE_RANKING;
import static org.osgi.framework.Constants.SERVICE_VENDOR;

/**
 * A wrapper class around OSGi service references
 */
public final class ServiceImpl implements Service
{
    private final ServiceReference service;
    private final PackageAccessor packageAccessor;

    ServiceImpl(ServiceReference service, PackageAccessor packageAccessor)
    {
        this.service = service;
        this.packageAccessor = packageAccessor;
    }

    public Bundle getBundle()
    {
        return BundleImpl.wrap(packageAccessor).fromSingleton(service.getBundle());
    }

    public Iterable<Bundle> getUsingBundles()
    {
        return BundleImpl.wrap(packageAccessor).fromArray(service.getUsingBundles());
    }

    public Iterable<String> getObjectClasses()
    {
        return getStringIterableProperty(OBJECTCLASS);
    }

    @Nullable
    public String getDescription()
    {
        return getNullableStringProperty(SERVICE_DESCRIPTION);
    }

    public long getId()
    {
        return toLong(service.getProperty(SERVICE_ID), 0);
    }

    public Iterable<String> getPid()
    {
        return getStringIterableProperty(SERVICE_PID);
    }

    public int getRanking()
    {
        return toInteger(service.getProperty(SERVICE_RANKING), 0);
    }

    @Nullable
    public String getVendor()
    {
        return getNullableStringProperty(SERVICE_VENDOR);
    }

    protected static Wrapper<ServiceReference, Service> wrap(final PackageAccessor packageAccessor)
    {
        return new Wrapper<ServiceReference, Service>("service")
        {
            protected Service wrap(ServiceReference service)
            {
                return new ServiceImpl(service, packageAccessor);
            }
        };
    }

    @Nullable
    private String getNullableStringProperty(String key)
    {
        return OsgiUtil.toString(service.getProperty(key), null);
    }

    private Iterable<String> getStringIterableProperty(String key)
    {
        return ImmutableList.copyOf(asList(toStringArray(service.getProperty(key), new String[0])));
    }
}
