/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;

public class HandlerFactory
{
    private static final Logger log = Logger.getLogger(HandlerFactory.class);

    public static MessageHandler getHandler(String clazz)
    {
        try
        {
            final PluginAccessor pluginAccessor = ComponentAccessor.getPluginAccessor();
            return (MessageHandler) ClassLoaderUtils.loadClass(clazz, pluginAccessor.getClassLoader()).newInstance();
        }
        catch (ClassNotFoundException e)
        {
            log.error("Could not getRequest message handler with class: " + clazz + ": " + e, e);
        }
        catch (Exception e)
        {
            log.error("Could not getRequest message handler with class: " + clazz + ": " + e, e);
        }

        return null;
    }
}
