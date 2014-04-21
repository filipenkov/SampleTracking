/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.template.Template;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericEntityException;
import webwork.action.ActionContext;

import java.util.Map;

@WebSudoRequired
public class AddNotification extends SchemeAwareNotificationAction
{
    private String type;
    private Long[] eventTypeIds;
    NotificationTypeManager ntm = ManagerFactory.getNotificationTypeManager();

    public SchemeManager getSchemeManager()
    {
        return ManagerFactory.getNotificationSchemeManager();
    }

    public String getRedirectURL()
    {
        return "EditNotifications!default.jspa?schemeId=";
    }

    protected void doValidation()
    {
        try
        {
            if (getSchemeId() == null || getScheme() == null)
                addErrorMessage(getText("admin.errors.notifications.must.select.scheme"));
            if (getEventTypeIds() == null || getEventTypeIds().length == 0)
                addError("eventTypeIds", getText("admin.errors.notifications.must.select.notification.to.add"));
            if (!TextUtils.stringSet(getType()))
                addErrorMessage(getText("admin.errors.notifications.must.select.type"));
            else if (!ntm.getNotificationType(getType()).doValidation(getType(), getParameters()))
                addErrorMessage(getText("admin.errors.notifications.fill.out.box"));
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("admin.errors.notifications.error.occured", "\n") + e.getMessage());
        }
    }

    protected String doExecute() throws Exception
    {
        for (int i = 0; i < eventTypeIds.length; i++)
        {
            Long eventTypeId = eventTypeIds[i];
            SchemeEntity schemeEntity = new SchemeEntity(getType(), getParameter(getType()), eventTypeId, null);
            String param = getParameter(getType());

            //prevent adding the same event multiple times
            if (!ManagerFactory.getNotificationSchemeManager().hasEntities(getScheme(), eventTypeId, type, param, null))
            {
                ManagerFactory.getNotificationSchemeManager().createSchemeEntity(getScheme(), schemeEntity);
            }
        }

        return getRedirect(getRedirectURL() + getSchemeId());
    }

    public Map getTypes()
    {
        return ntm.getTypes();
    }

    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    public Map getParameters()
    {
        return ActionContext.getSingleValueParameters();
    }

    public String getParameter(String key)
    {
        return (String) getParameters().get(key);
    }

    public Map getEvents()
    {
        return ComponentAccessor.getEventTypeManager().getEventTypesMap();
    }

    public Map getTemplates()
    {
        return ComponentManager.getInstance().getTemplateManager().getTemplatesMap(Template.TEMPLATE_TYPE_ISSUEEVENT);
    }

    public Long[] getEventTypeIds()
    {
        return eventTypeIds;
    }

    public void setEventTypeIds(Long[] eventTypeIds)
    {
        this.eventTypeIds = eventTypeIds;
    }
}
