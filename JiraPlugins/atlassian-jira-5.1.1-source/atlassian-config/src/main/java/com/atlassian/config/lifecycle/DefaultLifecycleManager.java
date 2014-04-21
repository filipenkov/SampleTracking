package com.atlassian.config.lifecycle;

import com.atlassian.config.lifecycle.events.ApplicationStartedEvent;
import com.atlassian.config.lifecycle.events.ApplicationStoppedEvent;
import com.atlassian.config.lifecycle.events.ApplicationStoppingEvent;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.plugin.PluginManager;
import org.apache.log4j.Logger;

import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.List;

public class DefaultLifecycleManager implements LifecycleManager
{
    private static Logger log = Logger.getLogger(LifecycleManager.class);

    private PluginManager pluginManager;
    private EventPublisher eventPublisher;

    public void startUp(ServletContext servletContext)
    {
        final List<LifecyclePluginModuleDescriptor> moduleDescriptors = getLifecyclePluginModuleDescriptors();
        final LifecycleContext context = new DefaultLifecycleContext(servletContext);

        LifecyclePluginModuleDescriptor currentDescriptor = null;
        try
        {
            for (LifecyclePluginModuleDescriptor descriptor : moduleDescriptors)
            {
                currentDescriptor = descriptor;
                log.info("Starting: " + descriptor);
                ((LifecycleItem) descriptor.getModule()).startup(context);
            }

            eventPublisher.publish(new ApplicationStartedEvent(this));
        }
        catch (Throwable t)
        {
            panicAndShutdown(t, context, currentDescriptor);
        }
    }

    public void shutDown(ServletContext servletContext)
    {
        shutDown(servletContext, null);
    }

    private void panicAndShutdown(Throwable t, LifecycleContext context, LifecyclePluginModuleDescriptor descriptor)
    {
        final String errorString = "Unable to start up Confluence. Fatal error during startup sequence: " + descriptor + " - " + t;
        log.fatal(errorString, t);
        context.getAgentJohnson().addEvent(new Event(EventType.get("startup"), errorString, EventLevel.FATAL));
        shutDown(context.getServletContext(), descriptor.getCompleteKey());
    }

    private void shutDown(ServletContext servletContext, String startingPluginKey)
    {
        eventPublisher.publish(new ApplicationStoppingEvent(this));

        final List<LifecyclePluginModuleDescriptor> moduleDescriptors = getLifecyclePluginModuleDescriptors();
        Collections.reverse(moduleDescriptors);
        final LifecycleContext context = new DefaultLifecycleContext(servletContext);

        boolean started = startingPluginKey == null;

        for (LifecyclePluginModuleDescriptor descriptor : moduleDescriptors)
        {
            if (!started)
            {
                if (descriptor.getCompleteKey().equals(startingPluginKey))
                {
                    started = true;
                }
                else
                {
                    continue;
                }
            }

            log.info("Shutting down: " + descriptor);
            final LifecycleItem item = (LifecycleItem) descriptor.getModule();
            try
            {
                item.shutdown(context);
            }
            catch (Throwable t)
            {
                log.error("Error running shutdown plugin: " + descriptor.getDescription() + " - " + t, t);
            }
        }

        eventPublisher.publish(new ApplicationStoppedEvent(this));
    }

    private List<LifecyclePluginModuleDescriptor> getLifecyclePluginModuleDescriptors()
    {
        final List<LifecyclePluginModuleDescriptor> modules = pluginManager.getEnabledModuleDescriptorsByClass(LifecyclePluginModuleDescriptor.class);
        Collections.sort(modules);
        return modules;
    }

    public void setPluginManager(PluginManager pluginManager)
    {
        this.pluginManager = pluginManager;
    }

    public void setEventPublisher(EventPublisher eventPublisher)
    {
        this.eventPublisher = eventPublisher;
    }
}
