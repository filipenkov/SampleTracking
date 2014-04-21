/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.extensions;

import com.atlassian.plugin.PluginAccessor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

public class PluggableMailHandlerUtils
{
    @Nullable
    public static MessageHandlerModuleDescriptor getHandlerKeyByMessageHandler(@Nonnull PluginAccessor pluginAccessor, @Nonnull String messageHandler)
    {
        final List<MessageHandlerModuleDescriptor> descriptors = pluginAccessor
                .getEnabledModuleDescriptorsByClass(MessageHandlerModuleDescriptor.class);
        for(MessageHandlerModuleDescriptor descriptor : descriptors) {
            if (messageHandler.equals(descriptor.getMessageHandler().getName())) {
                return descriptor;
            }
        }
        return null;
    }
}

