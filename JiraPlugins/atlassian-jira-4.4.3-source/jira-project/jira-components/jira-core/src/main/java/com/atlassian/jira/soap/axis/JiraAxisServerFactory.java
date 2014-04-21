package com.atlassian.jira.soap.axis;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.rpc.SoapModuleDescriptor;
import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.configuration.SimpleProvider;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.server.AxisServer;
import org.apache.axis.server.DefaultAxisServerFactory;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JiraAxisServerFactory extends DefaultAxisServerFactory
{
    private static final Logger log = Logger.getLogger(JiraAxisServerFactory.class);

    public AxisServer getServer(Map environment) throws AxisFault
    {
        EngineConfiguration defaultConfig = null;

        if (environment != null)
        {
            try
            {
                defaultConfig = (EngineConfiguration) environment.get(EngineConfiguration.PROPERTY_NAME);
            }
            catch (ClassCastException e)
            {
                log.warn(e, e);
                // Fall through
            }
        }
        else
        {
            environment = new HashMap();
        }

        SimpleProvider newConfig = new SimpleProvider(defaultConfig);
        List soapDescriptors = ComponentAccessor.getPluginAccessor().getEnabledModuleDescriptorsByClass(SoapModuleDescriptor.class);

        for (Iterator iterator = soapDescriptors.iterator(); iterator.hasNext();)
        {
            try
            {
                SoapModuleDescriptor descriptor = (SoapModuleDescriptor) iterator.next();

                if (log.isInfoEnabled())
                {
                    log.info("Publishing to " + descriptor.getServicePath() + " module " + descriptor.getModuleClass() + " with interface " + descriptor.getPublishedInterface());
                }

                SOAPService soapService = new JiraAxisSoapService(descriptor);
                newConfig.deployService(soapService.getName(), soapService);
            }
            catch (Throwable e)
            {
                log.warn("Error registering soap service: " + e, e);
            }
        }

        environment.put(EngineConfiguration.PROPERTY_NAME, newConfig);

        return super.getServer(environment);
    }
}
