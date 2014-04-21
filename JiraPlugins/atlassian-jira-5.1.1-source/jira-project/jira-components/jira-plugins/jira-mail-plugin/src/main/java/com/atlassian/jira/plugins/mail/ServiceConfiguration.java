/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail;

import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.collect.Maps;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;

public class ServiceConfiguration
{
    public static final String ID = ServiceConfiguration.class.getName();
    private String handlerKey;
    private String serviceName;
    private long delay;
    private String folder;
    private Long serverId;
    private Long serviceId;

    @Nullable
    public Long getServiceId()
    {
        return serviceId;
    }

    public void setServiceId(@Nullable Long serviceId)
    {
        this.serviceId = serviceId;
    }

    public void setHandlerKey(@Nonnull String handler)
    {
        this.handlerKey = handler;
    }

    @Nonnull
    public String getHandlerKey()
    {
        return handlerKey;
    }

    @Nonnull
    public String getServiceName()
    {
        return serviceName;
    }

    public void setServiceName(@Nonnull String serviceName)
    {
        this.serviceName = serviceName;
    }

    public long getDelay()
    {
        return delay;
    }

    public void setDelay(long delay)
    {
        this.delay = delay;
    }

    public void setFolder(@Nullable String folder)
    {
        this.folder = folder;
    }

    @Nullable
    public String getFolder()
    {
        return folder;
    }

    public void setServerId(@Nullable Long serverId)
    {
        this.serverId = serverId;
    }

    /**
     *
     * @return server id or null in case of file server
     */
    @Nullable
    public Long getServerId()
    {
        return serverId;
    }

    @Nonnull
    public Map<String, String[]> toServiceParams(@Nonnull PluginAccessor pa) throws MailException {
        return Maps.transformValues(toMap(pa), new Function<String, String[]>()
        {
            @Override
            public String[] apply(@Nullable String from)
            {
                return new String[] { from };
            }
        });
    }

    @Nonnull
    public Map<String, String> toMap(@Nonnull PluginAccessor pa) throws MailException {
        final MapBuilder<String, String> builder = MapBuilder.<String, String>newBuilder();
        final MessageHandlerModuleDescriptor enabledPluginModule = (MessageHandlerModuleDescriptor) pa.getEnabledPluginModule(getHandlerKey());
        if (enabledPluginModule != null) {
            builder.add(AbstractMessageHandlingService.KEY_HANDLER, enabledPluginModule.getMessageHandler().getName());
        }

        if (getServerId() != null) {
            builder.add(MailFetcherService.KEY_MAIL_SERVER, Long.toString(getServerId()))
                    .add(MailFetcherService.FOLDER_NAME_KEY, getFolder());
        } else {

            builder.add(FileService.KEY_SUBDIRECTORY, getFolder());
        }
        return builder.toMutableMap();
    }

    @Nonnull
    public String getServiceClass() {
        return (serverId == null ? FileService.class : MailFetcherService.class).getName();
    }
}
