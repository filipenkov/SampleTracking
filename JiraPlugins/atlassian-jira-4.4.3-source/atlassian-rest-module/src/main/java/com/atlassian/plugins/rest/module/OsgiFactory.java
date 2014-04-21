package com.atlassian.plugins.rest.module;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public interface OsgiFactory<S>
{
    S getInstance(BundleContext bundleContext, ServiceReference serviceReference);
}
