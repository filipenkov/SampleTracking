package com.atlassian.jira.web.servlet.rpc;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.rpc.XmlRpcModuleDescriptor;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
import com.atlassian.util.concurrent.ResettableLazyReference;
import org.apache.xmlrpc.XmlRpcServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * XML-RPC processor that uses handles provided by the plugin system.
 *
 * @since v4.4
 */
public class PluggableXmlRpcRequestProcessor implements XmlRpcRequestProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PluggableXmlRpcRequestProcessor.class);

    private final XmlRpcServerConfigurator serverConfigurator;

    public PluggableXmlRpcRequestProcessor(PluginAccessor pluginAccessor, EventPublisher eventPublisher) {
        this.serverConfigurator = new XmlRpcServerConfigurator(pluginAccessor);
        eventPublisher.register(serverConfigurator);
    }

    @Override
    public byte[] process(InputStream request) {
        return serverConfigurator.server().execute(request);
    }


    /**
     * Provides XML-RPC handler configuration from the plugin system.
     *
     */
    public static final class XmlRpcServerConfigurator
    {

        private final PluginAccessor pluginAccessor;
        private final ResettableLazyReference<XmlRpcServer> serverReference = new ResettableLazyReference<XmlRpcServer>()
        {
            @Override
            protected XmlRpcServer create() throws Exception
            {
                return createServer();
            }
        };


        private XmlRpcServerConfigurator(PluginAccessor pluginAccessor)
        {
            this.pluginAccessor = pluginAccessor;
        }

        private XmlRpcServer createServer()
        {
            final XmlRpcServer xmlrpc = new XmlRpcServer();
            try
            {
                addHandlers(xmlrpc);
            } catch (Throwable e)
            {
                logger.error("Error while initializing XML-RPC service", e);
            }
            return xmlrpc;
        }

        private void addHandlers(XmlRpcServer xmlrpc)
        {
            for (XmlRpcModuleDescriptor descriptor : pluginAccessor.getEnabledModuleDescriptorsByClass(XmlRpcModuleDescriptor.class))
            {
                if (logger.isInfoEnabled())
                {
                    logger.info("Publishing to " + descriptor.getServicePath() + " module " + descriptor.getModuleClass());
                }
                xmlrpc.addHandler(descriptor.getServicePath(), descriptor.getModule());
            }
        }

        public XmlRpcServer server()
        {
            return serverReference.get();
        }

        @EventListener
        public void onPluginModuleEnabled(PluginModuleEnabledEvent event)
        {
            onPluginModuleEvent(event.getModule());
        }

        @EventListener
        public void onPluginModuleDisabled(PluginModuleDisabledEvent event)
        {
            onPluginModuleEvent(event.getModule());
        }

        private void onPluginModuleEvent(ModuleDescriptor<?> moduleDescriptor)
        {
            if (moduleDescriptor instanceof XmlRpcModuleDescriptor)
            {
                serverReference.reset();
            }
        }
    }
}


