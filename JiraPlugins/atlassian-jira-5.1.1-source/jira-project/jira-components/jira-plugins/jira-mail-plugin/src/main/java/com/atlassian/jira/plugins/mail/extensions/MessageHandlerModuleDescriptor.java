/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.extensions;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.AbstractJiraModuleDescriptor;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.service.util.handler.MessageHandler;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.StateAware;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MessageHandlerModuleDescriptor extends AbstractJiraModuleDescriptor<MessageHandler> implements StateAware
{
    private Class<? extends MessageHandler> messageHandler;
    private String addEditUrl;
    private final ComponentClassManager componentClassManager;
    private int weight = Integer.MAX_VALUE;
    /** in case memory is not flushed after enabled() but before the getter is called */
    private volatile MessageHandlerValidator validator;
    private String validatorClassStr;

    public MessageHandlerModuleDescriptor(JiraAuthenticationContext authenticationContext, ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.componentClassManager = getComponentClassManager();
    }

    /*
     * ComponentClassManager is not injected, going to take it manually
     */
    @Nonnull
    protected ComponentClassManager getComponentClassManager()
    {
        return ComponentAccessor.getComponentClassManager();
    }


    @Override
    public void enabled()
    {
        super.enabled();
        // done only here because dependency injection is not guaranteed to work earlier.
        if (validatorClassStr != null)
        {
            try
            {
                validator = componentClassManager.newInstance(validatorClassStr);
            }
            catch (ClassNotFoundException e)
            {
                throw new PluginParseException("Cannot instatiate message-handler validator class '"
                        + validatorClassStr + "'", e);
            }
        }

    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        final String messageHandlerClass = element.attributeValue("class");
        try
        {
            this.messageHandler = componentClassManager.loadClass(messageHandlerClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new PluginParseException("Cannot load message-handler class '"
                    + messageHandlerClass + "'", e);
        }
        this.addEditUrl = element.attributeValue("add-edit-url");
        validatorClassStr = element.attributeValue("validator-class");

        final String weightString = element.attributeValue("weight");
        if (weightString != null)
        {
            try
            {
                this.weight = Integer.valueOf(weightString);
            }
            catch (NumberFormatException e)
            {
                throw new PluginParseException(
                        String.format("Invalid value for weight, must be an integer: %s", weight), e);
            }
        }
    }

    @Override
    public void disabled()
    {
        validator = null;
        super.disabled();
    }

    public Class<? extends MessageHandler> getMessageHandler()
    {
        return this.messageHandler;
    }

    /**
     *
     * @return an optional validator which instance is instatiated with dependency injection
     * and which additionally check whether given handler is valid for the current settings
     * Validator should be state-less, as there is only single instance created for given plugin module declaration
     */
    @Nullable
    public MessageHandlerValidator getValidator()
    {
        return validator;
    }

    public String getAddEditUrl()
    {
        return addEditUrl;
    }

    public int getWeight()
    {
        return weight;
    }
}
