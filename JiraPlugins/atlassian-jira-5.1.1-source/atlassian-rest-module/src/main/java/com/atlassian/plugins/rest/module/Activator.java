package com.atlassian.plugins.rest.module;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.osgi.context.BundleContextAware;

public class Activator implements BundleContextAware, InitializingBean, DisposableBean
{
    private final BundleActivator coreActivator = new com.sun.jersey.core.osgi.Activator();
    private final BundleActivator serverActivator = new com.sun.jersey.server.osgi.Activator(); 
    private BundleContext bundleContext;

    public void afterPropertiesSet() throws Exception
    {
        coreActivator.start(bundleContext);
        serverActivator.start(bundleContext);
    }

    public void destroy() throws Exception
    {
        serverActivator.stop(bundleContext);
        coreActivator.stop(bundleContext);
    }

    public void setBundleContext(BundleContext bundleContext)
    {
        this.bundleContext = bundleContext;
    }
}
