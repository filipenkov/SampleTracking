package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.projectconfig.util.TabUrlFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.PluginParseException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * @since v4.4
 */
public class NotificationsSummaryPanelContextProvider implements CacheableContextProvider
{
    private static final String NOTIFICATION_SCHEME = "notificationScheme";
    private static final String PROJECT_EMAIL = "projectEmail";
    private static final String HAS_CONFIGURED_MAIL_SERVER = "hasConfiguredMailServer";
    private static final String MANAGE_URL = "manageUrl";
    private final NotificationSchemeManager notificationSchemeManager;
    private final ContextProviderUtils providerUtils;
    private final MailServerManager mailServerManager;
    private final TabUrlFactory tabUrlFactory;

    public NotificationsSummaryPanelContextProvider(final NotificationSchemeManager notificationSchemeManager,
            final ContextProviderUtils providerUtils, final MailServerManager mailServerManager, TabUrlFactory tabUrlFactory)
    {
        this.notificationSchemeManager = notificationSchemeManager;
        this.providerUtils = providerUtils;
        this.mailServerManager = mailServerManager;
        this.tabUrlFactory = tabUrlFactory;
    }

    public void init(Map<String, String> stringStringMap) throws PluginParseException
    {
    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {

        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);

        final GenericValue projectGV = getProjectGV();
        final GenericValue notificationGV = notificationSchemeManager.getNotificationSchemeForProject(projectGV);

        if (notificationGV != null)
        {
            SimpleNotificationScheme notificationScheme = gvToNotificationScheme(notificationGV);
            contextMap.add(NOTIFICATION_SCHEME, notificationScheme);
        }

        contextMap.add(MANAGE_URL, tabUrlFactory.forNotifications());
        try
        {
            if (hasDefaultSMTPMailServer())
            {
                final String projectEmail = getProjectEmail(projectGV);
                final String defaultEmail = getDefaultEmail();

                contextMap.add(HAS_CONFIGURED_MAIL_SERVER, true);

                if (projectEmail != null)
                {
                    contextMap.add(PROJECT_EMAIL, projectEmail);
                }
                else if (defaultEmail != null)
                {
                    contextMap.add(PROJECT_EMAIL, defaultEmail);
                }
            }
        }
        catch (MailException e)
        {
            throw new RuntimeException(e);
        }

        return contextMap.toMap();
    }

    private SimpleNotificationScheme gvToNotificationScheme(final GenericValue notificationGV)
    {
        return new SimpleNotificationScheme(notificationGV.getString("id"), notificationGV.getString("description"),
                notificationGV.getString("name"));
    }

    private boolean hasDefaultSMTPMailServer() throws MailException
    {
        return (MailFactory.getServerManager().getDefaultSMTPMailServer() != null);
    }

    private String getProjectEmail(GenericValue projectGV) throws MailException
    {
        return OFBizPropertyUtils.getPropertySet(projectGV).getString(ProjectKeys.EMAIL_SENDER);
    }

    private String getDefaultEmail() throws MailException
    {
        final SMTPMailServer defaultSMTPMailServer = mailServerManager.getDefaultSMTPMailServer();

        if (defaultSMTPMailServer != null)
        {
            return defaultSMTPMailServer.getDefaultFrom();
        }

        return null;
    }

    private GenericValue getProjectGV()
    {
        return providerUtils.getProject().getGenericValue();
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class SimpleNotificationScheme
    {

        private final String id;
        private final String description;
        private final String name;

        SimpleNotificationScheme(final String id, final String description, final String name)
        {
            this.id = id;
            this.description = description;
            this.name = name;
        }

        public String getId()
        {
            return id;
        }

        public String getDescription()
        {
            return description;
        }

        public String getName()
        {
            return name;
        }

    }

}
