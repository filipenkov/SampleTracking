package com.atlassian.plugins.rest.module.template;

import com.atlassian.templaterenderer.velocity.one.six.VelocityTemplateRenderer;
import com.sun.jersey.spi.template.TemplateProcessor;
import org.osgi.framework.Bundle;
import org.osgi.framework.ServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.tracker.ServiceTracker;

import java.io.IOException;
import java.io.OutputStream;

/**
 * OSGi Service Factory for creating velocity template processors, with the right bundle context.
 */
//////////////////
// TODO The TemplateProcessor implementation is a workaround for a Spring DM bug that is fixed in
// version 1.2.0.  It can be removed once the plugins system has upgraded to 1.2.0
/////////////////
public class VelocityTemplateProcessorServiceFactory implements ServiceFactory, TemplateProcessor
{
    public Object getService(Bundle bundle, ServiceRegistration serviceRegistration)
    {
        // Create a ServiceTracker for the template renderer.  This is needed because the template
        // renderer may not yet be started, or it may come up or go down
        ServiceTracker serviceTracker = new ServiceTracker(bundle.getBundleContext(), VelocityTemplateRenderer.class.getName(), null);
        serviceTracker.open();
        return new VelocityTemplateProcessor(serviceTracker);
    }

    public void ungetService(Bundle bundle, ServiceRegistration serviceRegistration, Object service)
    {
        ((VelocityTemplateProcessor) service).closeTemplateRendererServiceTracker();
    }

    public String resolve(String s)
    {
        throw new UnsupportedOperationException();
    }

    public void writeTo(String s, Object o, OutputStream outputStream) throws IOException
    {
        throw new UnsupportedOperationException();
    }
}
