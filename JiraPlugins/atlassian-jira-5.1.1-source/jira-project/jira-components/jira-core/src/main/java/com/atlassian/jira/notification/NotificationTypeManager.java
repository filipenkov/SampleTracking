package com.atlassian.jira.notification;

import com.atlassian.jira.scheme.AbstractSchemeTypeManager;

import java.util.Map;

public class NotificationTypeManager extends AbstractSchemeTypeManager<NotificationType>
{
    Map<String, NotificationType> schemeTypes;
    private String configFile = "notification-event-types.xml";

    public NotificationTypeManager()
    {
    }

    public NotificationTypeManager(String configFile)
    {
        this.configFile = configFile;
    }

    public String getResourceName()
    {
        return configFile;
    }

    public Class getTypeClass()
    {
        return NotificationTypeManager.class;
    }

    public NotificationType getNotificationType(String id)
    {
        return (NotificationType) getTypes().get(id);
    }

    public Map<String, NotificationType> getSchemeTypes()
    {
        return schemeTypes;
    }

    public void setSchemeTypes(Map<String, NotificationType> schemeType)
    {
        schemeTypes = schemeType;
    }
}
