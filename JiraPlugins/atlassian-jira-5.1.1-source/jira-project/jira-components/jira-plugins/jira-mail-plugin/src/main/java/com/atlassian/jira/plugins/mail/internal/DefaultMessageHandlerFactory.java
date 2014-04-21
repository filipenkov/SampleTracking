package com.atlassian.jira.plugins.mail.internal;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.plugins.mail.upgrade.UpgradeTask_1_MoveMailHandlers;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.jira.service.util.handler.MessageHandlerFactory;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.classloader.PluginsClassLoader;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.log4j.Logger;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Class responsible for instantiating message handlers
 *
 * @since v5.0
 */
public class DefaultMessageHandlerFactory implements MessageHandlerFactory
{
    private final ComponentClassManager componentClassManager;
    private final PluginAccessor pluginAccessor;

    private final Logger log;

	public DefaultMessageHandlerFactory(MailLoggingManager mailLoggingManager, PluginAccessor pluginAccessor) {
        this.pluginAccessor = pluginAccessor;
        this.componentClassManager = ComponentAccessor.getComponentClassManager();
        log = mailLoggingManager.getIncomingMailChildLogger(DefaultMessageHandlerFactory.class.getSimpleName());
    }

    protected boolean isStudio() {
        // you can call this after studio was brough up, we're part of studio, during initialization it's not yet set
        return ComponentAccessor.getComponent(FeatureManager.class).isEnabled(com.atlassian.jira.config.CoreFeatures.ON_DEMAND);
    }

    @Override
    public MessageHandler getHandler(final String clazz)
    {
        if (UpgradeTask_1_MoveMailHandlers.handlerTranslation.containsKey(clazz))
        {
            log.error("Not instantiating the obsolete class [" + clazz + "]. Please ignore this message during restore from a backup or JIRA upgrade, the service configuration should be upgraded later during the process.");
            log.error("If you are still seeing this after JIRA has been fully upgraded please reconfigure your mail handling services using the Incoming Mail Servers screen.");
            return null;
        }

        try
        {
            final Iterable<MessageHandlerModuleDescriptor> corespondingDescriptors = Iterables.filter(pluginAccessor.getEnabledModuleDescriptorsByClass(MessageHandlerModuleDescriptor.class),
                    new Predicate<MessageHandlerModuleDescriptor>()
            {
                @Override
                public boolean apply(@Nullable MessageHandlerModuleDescriptor descriptor)
                {
                    return descriptor != null && descriptor.getMessageHandler().getName().equals(clazz);
                }
            });

            final Iterator<MessageHandlerModuleDescriptor> iterator = corespondingDescriptors.iterator();

            if (!iterator.hasNext())
            {
                 log.error("Handler '" + clazz + "' cannot be instantiated because there is no corresponding enabled module defining message-handler of such class");
                 return null;
            }

            final MessageHandlerModuleDescriptor descriptor = iterator.next();
            final Class<MessageHandler> messageHandlerClass = descriptor.getPlugin().loadClass(clazz, getClass());

            final Object handler = componentClassManager.newInstanceFromPlugin(messageHandlerClass, descriptor.getPlugin());

            if (handler == null) // should never happen, but the contract of newInstance above is quite weak
            {
                log.error("Could not instantiate message handler with class: " + clazz + ": null returned.");
                return null;
            }
            if (!MessageHandler.class.isInstance(handler)) {
                log.error("Cannot instantiate message handler of requested class '" + clazz + "'. Expected "
                        + "an instance of '" + MessageHandler.class.getName() + "' class, but found '"
                        + handler.getClass().getName() + "'.");
                return null;
            }
            return (MessageHandler) handler;
        }
        catch (ClassNotFoundException e)
        {
            log.error("Message handler class [" + clazz + "] not found. Please make sure the plugin providing this class is enabled or reconfigure your mail handling services using the Incoming Mail Servers screen.", e);
        }
        catch (Exception e)
        {
            log.error("Could not instantiate message handler with class: " + clazz, e);
        }

        return null;
    }

    @Nullable
    @Override
    public String getCorrespondingModuleDescriptorKey(final String clazz)
    {
        final Plugin pluginForClass = ((PluginsClassLoader) ComponentAccessor.getPluginAccessor().getClassLoader()).getPluginForClass(clazz);
        if (pluginForClass == null) {
            return null;
        }
        try
        {
            return Iterables.find(Iterables.filter(pluginForClass.getModuleDescriptors(), MessageHandlerModuleDescriptor.class), new Predicate<MessageHandlerModuleDescriptor>()
            {
                @Override
                public boolean apply(@Nullable MessageHandlerModuleDescriptor input)
                {
                    return input != null && (clazz.equals(input.getMessageHandler().getName()));
                }
            }).getCompleteKey();
        }
        catch (NoSuchElementException e)
        {
            return null;
        }
    }

}
