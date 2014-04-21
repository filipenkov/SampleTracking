/*
 * Copyright (C) 2002-2011 Atlassian
 * All rights reserved.
 */
package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.jira.plugins.mail.ImapServersValuesGenerator;
import com.atlassian.jira.plugins.mail.PopServersValuesGenerator;
import com.atlassian.jira.plugins.mail.ServiceConfiguration;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerValidator;
import com.atlassian.jira.plugins.mail.extensions.PluggableMailHandlerUtils;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.services.mail.MailFetcherService;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class EditServerDetailsWebAction extends MailWebActionSupport
{
    private String handler;
    private String mailServer;
    private String serviceName;
    private String folder;
    private Long delay = 1l;
    private Long serviceId;
    private MessageHandlerModuleDescriptor descriptor;

    private final ProjectManager projectManager;
    private final PluginAccessor pluginAccessor;

    public EditServerDetailsWebAction(ProjectManager projectManager, PluginAccessor pluginAccessor)
    {
        this.projectManager = projectManager;
        this.pluginAccessor = pluginAccessor;
    }

    @Override
    public String doDefault() throws Exception
    {
        String result = super.doDefault();

        ActionContext.getSession().put(ServiceConfiguration.ID, null);

        if (serviceId != null)
        {
            if (canEditService(serviceId))
            {
                final JiraServiceContainer service = getService(serviceId);
                if (service != null)
                {
                    serviceName = service.getName();
                    delay = service.getDelay() / 60000;
                    mailServer = service.getProperty(MailFetcherService.KEY_MAIL_SERVER);
                    if (MailFetcherService.class.getName().equals(service.getServiceClass())) {
                        folder = service.getProperty(MailFetcherService.FOLDER_NAME_KEY);
                    } else {
                        folder = service.getProperty(FileService.KEY_SUBDIRECTORY);
                    }
                    handler = getHandlerKey(service.getProperty(AbstractMessageHandlingService.KEY_HANDLER));
                }
                else
                {
                    return returnCompleteWithInlineRedirect(ViewMailServers.INCOMING_MAIL_ACTION);
                }
            }
            else
            {
                return "securitybreach";
            }
        }

        return result;
    }

    @Nullable
    private String getHandlerKey(@Nonnull String messageHandler)
    {
        MessageHandlerModuleDescriptor descriptor = PluggableMailHandlerUtils.getHandlerKeyByMessageHandler(pluginAccessor, messageHandler);
        return descriptor != null ? descriptor.getCompleteKey() : null;
    }

    @Override
    protected void doValidation()
    {
        super.doValidation();

        if (StringUtils.isBlank(serviceName))
        {
            addError("serviceName", getText("admin.errors.specify.service.name"));
        }

        if (delay == null)
        {
            addError("delay", getText("EMPTY_LONG"));
        }
        else if (delay < 1)
        {
            addError("delay", getText("admin.errors.delay.too.short"));
        }

        // JRADEV-8625 Non-system admins are required to select a server, Local Files are not available for them
        if (StringUtils.isNotBlank(mailServer) || !isSystemAdministrator()) {
            try
            {
                Long.parseLong(mailServer);
            }
            catch (Exception e)
            {
                addError("mailServer", getText("jmp.editServerDetails.please.select.server"));
            }
        }

        try
        {
            descriptor = (MessageHandlerModuleDescriptor) pluginAccessor.getEnabledPluginModule(handler);
        }
        catch (Exception e)
        {
            addError("handler", e.getMessage());
        }

        if (descriptor != null)
        {
            final MessageHandlerValidator validator = descriptor.getValidator();
            if (validator != null)
            {
                final ErrorCollection errorCollection = validator.validate();
                if (errorCollection.hasAnyErrors())
                {
                    addError("handler", StringUtils.join(errorCollection.getErrorMessages(), "\n"));

                }
            }
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ServiceConfiguration configuration = new ServiceConfiguration();

        configuration.setHandlerKey(getHandler());
        if (StringUtils.isNotBlank(mailServer)) {
            configuration.setServerId(Long.parseLong(mailServer));
        }
        configuration.setDelay(delay);
        configuration.setServiceName(serviceName);
        configuration.setServiceId(serviceId);

        if (!StringUtils.isBlank(folder))
        {
            configuration.setFolder(folder);
        }

        if ((isEditing() && !canEditService(serviceId))
                || (!isEditing() && !canAddService(configuration.getServiceClass())))
        {
            return "securitybreach";
        }

        ActionContext.getSession().put(ServiceConfiguration.ID, configuration);

        String url = descriptor.getAddEditUrl();
        if (isInlineDialogMode()) {
            url += (url.contains("?") ? "&" : "?") + "decorator=dialog&inline=true";
        }
        return getRedirect(url);
    }

    public String getHandler()
    {
        return handler;
    }

    @SuppressWarnings ("unused")
    public void setHandler(String handler)
    {
        this.handler = handler;
    }

    public String getMailServer()
    {
        return mailServer;
    }

    @SuppressWarnings ("unused")
    public void setMailServer(String mailServer)
    {
        this.mailServer = mailServer;
    }

    @Nonnull
    public Map<String, String> getImapServers()
    {
        return new ImapServersValuesGenerator().getValues(Collections.emptyMap());
    }

    @Nonnull
    public Map<String, String> getPopServers()
    {
        return new PopServersValuesGenerator().getValues(Collections.emptyMap());
    }

    @Nonnull
    public Map<String, String> getHandlers()
    {
        final LinkedHashMap<String, String> handlers = Maps.newLinkedHashMap();
        final List<MessageHandlerModuleDescriptor> descriptors = pluginAccessor
                .getEnabledModuleDescriptorsByClass(MessageHandlerModuleDescriptor.class);

        final Comparator<MessageHandlerModuleDescriptor> compareWeights = new Comparator<MessageHandlerModuleDescriptor>()
        {
            @Override
            public int compare(MessageHandlerModuleDescriptor o1, MessageHandlerModuleDescriptor o2)
            {
                return ComparisonChain.start().compare(o1.getWeight(), o2.getWeight())
                        .compare(o1.getName(), o2.getName()).result();
            }
        };

        for (MessageHandlerModuleDescriptor descriptor : ImmutableSortedSet.copyOf(compareWeights, descriptors)) {
            handlers.put(descriptor.getCompleteKey(), descriptor.getName());
        }
        return handlers;
    }

    @Nonnull
    public Map<String, Map<String, String>> getMailServers()
    {
        final Map<String, String> imapServers = getImapServers();
        final Map<String, String> popServers = getPopServers();

        final ImmutableMap.Builder<String, Map<String, String>> builder = ImmutableMap.builder();

        if (!imapServers.isEmpty())
        {
            builder.put("IMAP", imapServers);
        }

        if (!popServers.isEmpty())
        {
            builder.put("POP3", popServers);
        }

        return builder.build();
    }

    public Long getDelay()
    {
        return delay;
    }

    @SuppressWarnings ("unused")
    public void setDelay(Long delay)
    {
        this.delay = delay;
    }

    public String getServiceName()
    {
        return serviceName;
    }

    @SuppressWarnings ("unused")
    public void setServiceName(String serviceName)
    {
        this.serviceName = serviceName;
    }

    public String getFolder()
    {
        return folder;
    }

    @SuppressWarnings ("unused")
    public void setFolder(String folder)
    {
        this.folder = folder;
    }

    @SuppressWarnings ("unused")
    public void setServiceId(Long serviceId)
    {
        this.serviceId = serviceId;
    }

    @Nullable
    public Long getServiceId()
    {
        return this.serviceId;
    }

    public boolean isEditing()
    {
        return serviceId != null;
    }

}
