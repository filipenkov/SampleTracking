package com.atlassian.jira.projectconfig.contextproviders;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.notification.ProjectNotificationsSchemeHelper;
import com.atlassian.jira.plugin.webfragment.CacheableContextProvider;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.server.MailServerManager;
import com.atlassian.mail.server.SMTPMailServer;
import com.atlassian.plugin.PluginParseException;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Context Provider for the notifications panel
 *
 * @since v4.4
 */
public class ProjectNotificationContextProvider implements CacheableContextProvider
{
    private static final String CONTEXT_SHARED_PROJECTS_KEY = "sharedProjects";
    private static final String CONTEXT_SCHEME_NAME = "schemeName";
    private static final String CONTEXT_SCHEME_ID = "schemeId";
    private static final String CONTEXT_SCHEME_DESCRIPTION = "schemeDescription";
    private static final String CONTEXT_NOTIFICATIONS = "notifications";
    private static final String CONTEXT_HAS_ADMIN_PERMISSION = "isAdmin";
    private static final String PROJECT_EMAIL = "projectEmail";

    private static final String HAS_CONFIGURED_MAIL_SERVER = "hasConfiguredMailServer";
    private static final String ENTITY_TYPE = "type";

    private static final String ENTITY_PARAMETER = "parameter";
    private static final String SCHEME_NAME = "name";
    private static final String SCHEME_ID = "id";

    private static final String SCHEME_DESCRIPTION = "description";
    private final NotificationTypeManager notificationTypeManager;
    private final NotificationSchemeManager notificationSchemeManager;
    private final ProjectNotificationsSchemeHelper helper;
    private final MailServerManager mailServerManager;
    private final ContextProviderUtils contextProviderUtils;
    private final JiraAuthenticationContext ctx;

    public ProjectNotificationContextProvider(NotificationSchemeManager notificationSchemeManager,
            final ContextProviderUtils contextProviderUtils, final ProjectNotificationsSchemeHelper helper, final MailServerManager mailServerManager)
    {
        this.contextProviderUtils = contextProviderUtils;
        this.notificationSchemeManager = notificationSchemeManager;
        this.helper = helper;
        this.mailServerManager = mailServerManager;
        this.notificationTypeManager = ComponentAccessor.getComponentOfType(NotificationTypeManager.class);
        this.ctx = ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    }

    public void init(Map<String, String> params) throws PluginParseException
    {

    }

    public Map<String, Object> getContextMap(Map<String, Object> context)
    {
        MapBuilder<String, Object> contextMap = MapBuilder.<String, Object>newBuilder().addAll(context);
        final Map<String, Object> defaultContext = contextProviderUtils.getDefaultContext();
        final GenericValue projectGV = getProjectGV();
        contextMap.addAll(defaultContext);

        final Project project = (Project) defaultContext.get(ContextProviderUtils.CONTEXT_PROJECT_KEY);

        final I18nHelper i18nHelper = (I18nHelper) defaultContext.get(ContextProviderUtils.CONTEXT_I18N_KEY);

        final GenericValue scheme = getNotificationScheme(project);

        if (scheme != null)
        {
            final Scheme notificationScheme = notificationSchemeManager.getSchemeObject(scheme.getLong("id"));
            final List<Project> sharedProjects = helper.getSharedProjects(notificationScheme);

            contextMap.add(CONTEXT_SHARED_PROJECTS_KEY, sharedProjects);
            contextMap.add(CONTEXT_SCHEME_NAME, scheme.getString(SCHEME_NAME));
            contextMap.add(CONTEXT_SCHEME_ID, scheme.getLong(SCHEME_ID));
            final String description = scheme.getString(SCHEME_DESCRIPTION);
            if (StringUtils.isNotBlank(description))
            {
                contextMap.add(CONTEXT_SCHEME_DESCRIPTION, description);
            }

            final List<Notification> notifications = getNotifications(scheme);

            contextMap.add(CONTEXT_NOTIFICATIONS, notifications);
        }
        else
        {
            contextMap.add(CONTEXT_SCHEME_NAME, i18nHelper.getText("common.words.none"));
        }
        try
        {
            if (hasDefaultSMTPMailServer())
            {
                contextMap.add(HAS_CONFIGURED_MAIL_SERVER, true);
            }
        }
        catch (MailException e)
        {
            throw new RuntimeException(e);
        }

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
        return contextProviderUtils.getProject().getGenericValue();
    }

    private List<Notification> getNotifications(GenericValue scheme)
    {
        Collection<EventType> eventTypes = getEventTypes();

        List<Notification> notificationList = new ArrayList<Notification>(eventTypes.size());
        for (EventType eventType : eventTypes)
        {
            final List<GenericValue> entities = getEntities(scheme, eventType.getId());
            final List<String> entityDisplays = new ArrayList<String>(entities.size());
            for (GenericValue entity : entities)
            {
                final String typeStr = entity.getString(ENTITY_TYPE);
                final NotificationType type = notificationTypeManager.getNotificationType(typeStr);
                final String paramater = entity.getString(ENTITY_PARAMETER);
                final StringBuilder sb = new StringBuilder(type.getDisplayName());
                if (StringUtils.isNotBlank(paramater))
                {
                    sb.append(" (").append(type.getArgumentDisplay(paramater)).append(")");
                }
                entityDisplays.add(sb.toString());
            }

            Collections.sort(entityDisplays);
            notificationList.add(new Notification(eventType.getId(), eventType.getTranslatedName(ctx.getLoggedInUser()), eventType.getTranslatedDesc(ctx.getLoggedInUser()), entityDisplays));
        }

        // EventTypes are pre-sorted by the EventTypeManager, so we don't try and resort them here as that would be just wrong.
        return  notificationList;
    }

    private List<GenericValue> getEntities(GenericValue scheme, Long eventTypeId)
    {
        try
        {
            return notificationSchemeManager.getEntities(scheme, eventTypeId);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    private Collection<EventType> getEventTypes()
    {
        return ComponentAccessor.getEventTypeManager().getEventTypesMap().values();
    }

    private GenericValue getNotificationScheme(final Project project)
    {
        return notificationSchemeManager.getNotificationSchemeForProject(project.getGenericValue());
    }

    private boolean hasDefaultSMTPMailServer() throws MailException
    {
        return (MailFactory.getServerManager().getDefaultSMTPMailServer() != null);
    }

    @Override
    public String getUniqueContextKey(Map<String, Object> context)
    {
        return getClass().getName();
    }

    public static class Notification
    {
        private final Long id;
        private final String name;
        private final String description;
        private final List<String> entities;

        public Notification(Long id, String name, String description, List<String> entities)
        {
            this.id= id;
            this.name = name;
            this.entities = entities;
            this.description = description;
        }

        public Long getId()
        {
            return id;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public List<String> getEntities()
        {
            return entities;
        }
    }
}
