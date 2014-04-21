/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.webwork;

import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.plugins.mail.extensions.MessageHandlerModuleDescriptor;
import com.atlassian.jira.plugins.mail.extensions.PluggableMailHandlerUtils;
import com.atlassian.jira.plugins.mail.handlers.CreateOrCommentHandler;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.service.JiraServiceContainer;
import com.atlassian.jira.service.ServiceManager;
import com.atlassian.jira.service.services.file.AbstractMessageHandlingService;
import com.atlassian.jira.service.services.file.FileService;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServer;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.tools.generic.EscapeTool;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

@WebSudoRequired
public class ViewMailServers extends MailServerActionSupport
{
    public static final String OUTGOING_MAIL_TAB = "outgoing_mail";
    public static final String INCOMING_MAIL_TAB = "incoming_mail";
    public static final String OUTGOING_MAIL_ACTION = "OutgoingMailServers.jspa";
    public static final String INCOMING_MAIL_ACTION = "IncomingMailServers.jspa";

    private final ServiceManager serviceManager;
    private final ComponentClassManager componentClassManager;
    private final ProjectManager projectManager;
    private final PluginAccessor pluginAccessor;
    private final ConstantsManager constantsManager;


    public ViewMailServers(ServiceManager serviceManager, ConstantsManager constantsManager,
            ProjectManager projectManager, PluginAccessor pluginAccessor) {
        this.serviceManager = serviceManager;
        this.constantsManager = constantsManager;
        this.projectManager = projectManager;
        this.pluginAccessor = pluginAccessor;
        this.componentClassManager = getComponentClassManager();
    }

    @Nonnull
    protected ComponentClassManager getComponentClassManager()
    {
        return ComponentAccessor.getComponentClassManager();
    }

    public String getInvalidPopSettingsMessage() {
        HelpUtil helpUtil = new HelpUtil();
        HelpUtil.HelpPath helpPath = helpUtil.getHelpPath("decodeparameters");

        return getText("admin.mailservers.mail.bad.props", "<a href=\"" + helpPath.getUrl() + "\">", "</a>");
    }

    public Collection<JiraServiceContainer> getMailHandlers() {
        final Iterable<JiraServiceContainer> services = serviceManager.getServicesManageableBy(getLoggedInUser());
        final class IsMailHandlerFilter implements Predicate<JiraServiceContainer>
        {
            @Override
            public boolean apply(JiraServiceContainer jiraServiceContainer)
            {
                try
                {
                    return AbstractMessageHandlingService.class.isAssignableFrom(componentClassManager.loadClass(jiraServiceContainer.getServiceClass()));
                }
                catch (ClassNotFoundException e)
                {
                    return false;
                }
            }
        }
        final ImmutableList<JiraServiceContainer> jiraServiceContainers = ImmutableList.copyOf(Iterables.filter(services, new IsMailHandlerFilter()));
        return jiraServiceContainers;
    }

    @Nullable
    public Project getRelatedProject(JiraServiceContainer service) {
        final String id = getRelatedProjectKey(service);
        return id == null ? null : projectManager.getProjectObjByKey(id);
    }

    @Nullable
    public String getRelatedProjectKey(JiraServiceContainer service)
    {
        // JRADEV-8515: upper case project key, because that's what we also do in CreateIssueHandler
        final Map<String, String> params = parseHandlerParams(service);
        final String project = params == null ? null : params.get(CreateOrCommentHandler.KEY_PROJECT);
        return project == null ? null : project.toUpperCase(Locale.getDefault());
    }

    @Nullable
    public String getRelatedIssueId(JiraServiceContainer service)
    {
        final Map<String, String> params = parseHandlerParams(service);
        return params != null ? StringUtils.trimToNull(params.get(CreateOrCommentHandler.KEY_ISSUETYPE)) : null;
    }

    @Nullable
    public IssueConstant getRelatedIssueType(JiraServiceContainer service)
    {
        final String issueType = getRelatedIssueId(service);
        return issueType == null ? null : constantsManager.getIssueTypeObject(issueType);
    }

    public boolean isHandlerUsingObsoleteSettings(JiraServiceContainer service) {
        final Map<String, String> params = parseHandlerParams(service);
        return params != null && (params.containsKey("port") || params.containsKey("usessl"));
    }

    @Nonnull
    public Collection<Pair<String, String>> getServiceParams(JiraServiceContainer service) {
        final Map<String, String> params = parseHandlerParams(service);
        if (params == null) {
            return Collections.singleton(Pair.of(getText("common.words.unknown"), ""));
        }

        params.remove("project");
        params.remove("issuetype");
        try
        {
            final String forwardEmail = service.getProperty("forwardEmail");
            if (!StringUtils.isBlank(forwardEmail)) {
                params.put("forwardEmail", forwardEmail);
            }
        }
        catch (ObjectConfigurationException e)
        {
            // ignore
        }

        Collection<Pair<String, String>> result = Lists.newArrayListWithCapacity(params.size());
        for (Map.Entry<String, String> label : EditHandlerDetailsWebAction.getFieldLabels().entrySet())
        {
            final String value = params.remove(label.getKey());
            if (value != null) {
                result.add(Pair.of(getText(label.getValue()), value));
            }
        }
        result.addAll(Collections2.transform(params.entrySet(), new Function<Map.Entry<String, String>, Pair<String, String>>()
        {
            @Override
            public Pair<String, String> apply(Map.Entry<String, String> from)
            {
                return Pair.of(from.getKey(), from.getValue());
            }
        }));

        return result;
    }

    @Nullable
    Map<String, String> parseHandlerParams(JiraServiceContainer service)
    {
        if (!service.isUsable()) {
            return null;
        }

        final String params;
        try
        {
            params = service.getProperty("handler.params");
            if (params == null)
            {
                return null;
            }
        }
        catch (ObjectConfigurationException e)
        {
            return null;
        }
        return ServiceUtils.getParameterMap(params);
    }

    @Nullable
    public MailServer getServer(JiraServiceContainer service)
    {
        if (!service.isUsable()) {
            return null;
        }

        try
        {
            final String popserver = service.getProperty("popserver");
            if (popserver == null) {
                return null;
            }
            final Long serverId = Long.parseLong(popserver);
            final MailServer mailServer = MailFactory.getServerManager().getMailServer(serverId);
            if (mailServer == null) {
                log.warn(String.format("Cannot find mail server with id %s", serverId));
            }
            return mailServer;
        }
        catch (Exception e)
        {
            log.warn("Cannot parse mail handler configuration", e);
            return null;
        }
    }

    @Nullable
    public String getServerName(JiraServiceContainer service) {
        final MailServer server = getServer(service);
        return server == null ? null : server.getName();
    }

    @Nonnull
    public String getServerDescription(JiraServiceContainer service)
    {
        final MailServer server = getServer(service);
        return server == null ? "" : server.getHostname();
    }

    @Nonnull
    public String getFileServiceDirectory(JiraServiceContainer service) {
        if (!service.isUsable()) {
            return "";
        }
        try
        {
            return StringUtils.defaultString(service.getProperty(FileService.KEY_SUBDIRECTORY), "");
        }
        catch (ObjectConfigurationException e)
        {
            return "";
        }
    }

    public String getHandlerType(JiraServiceContainer service)
    {
        if (!service.isUsable()) {
            return "";
        }

        final String handlerClass;
        try
        {
            handlerClass = service.getProperty(AbstractMessageHandlingService.KEY_HANDLER);
        }
        catch (ObjectConfigurationException e)
        {
            return "";
        }

        if (StringUtils.isBlank(handlerClass))
        {
            return "";
        }

        final MessageHandlerModuleDescriptor descriptor = PluggableMailHandlerUtils.getHandlerKeyByMessageHandler(pluginAccessor, handlerClass);

        return descriptor == null ? handlerClass : descriptor.getName();
    }

    public EscapeTool getEsc() {
        return new EscapeTool();
    }

    public HelpUtil.HelpPath getHelpPath(String key) {
        return new HelpUtil().getHelpPath(key);
    }

}
