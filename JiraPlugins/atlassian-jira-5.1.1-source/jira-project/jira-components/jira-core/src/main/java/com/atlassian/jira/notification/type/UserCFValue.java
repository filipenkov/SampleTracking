/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class UserCFValue extends AbstractNotificationType
{
    public static final String ID = "User_Custom_Field_Value";
    private static final Logger log = Logger.getLogger(UserCFValue.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final FieldManager fieldManager;

    public UserCFValue(JiraAuthenticationContext jiraAuthenticationContext, FieldManager fieldManager)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.fieldManager = checkNotNull(fieldManager);
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        try
        {
            Object recipients = getRecipientsObject(event, argument);
            List<User> users = convertToUsers(recipients);
            return Lists.transform(users, UserToRecipient.INSTANCE);
        }
        catch (Exception e)
        {
            log.warn("Exception occurred while working out recipients from a custom field value. Returning empty list.", e);
            return Collections.emptyList();
        }
    }

    private Object getRecipientsObject(IssueEvent event, String argument)
    {
        // first try to retrieve from event params
        Object answer = getFromEventParams(event, argument);
        if (answer != null)
        {
            return answer;
        }
        // fall back to issue
        final Issue issue = event.getIssue();
        final CustomField field = getField(argument);
        if (field != null && field.isInScope(issue.getProjectObject(), ImmutableList.of(issue.getIssueTypeObject().getId())))
        {
            return field.getValue(issue);
        }
        else
        {
            return null;
        }
    }

    @SuppressWarnings ( { "unchecked" })
    private List<User> convertToUsers(Object recipients)
    {
        List<User> users;
        if (recipients instanceof Iterable<?>)
        {
            users = ImmutableList.copyOf((Iterable<User>) recipients);
        }
        else if (recipients instanceof User)
        {
            users = ImmutableList.of((User)recipients);
        }
        else
        {
            users = Collections.emptyList();
        }
        return users;
    }

    private Object getFromEventParams(IssueEvent event, String argument)
    {
        @SuppressWarnings ( { "unchecked" }) Map<String, Object> customFields = (Map) event.getParams().get(IssueEvent.CUSTOM_FIELDS_PARAM_NAME);
        if (customFields == null)
        {
            return null;
        }
        else
        {
            return customFields.get(argument);
        }
    }

    private CustomField getField(String argument)
    {
        try
        {
            return fieldManager.getCustomField(argument);
        }
        catch (IllegalArgumentException e)
        {
            //JRA-14392: This shouldn't happen any longer, however there may still be some old invalid data that could
            //cause this.
            log.warn("Error while retrieving custom field. Returning empty list of e-mail recipients. "
                    + "Please remove any invalid custom fields from your notification schemes.", e);
            return null;
        }
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.user.custom.field.value");
    }

    public String getType()
    {
        return "userCF";
    }


    /**
     * Used in the UI layer. Don't remove!
     *
     * @return list of all {@link com.atlassian.jira.notification.type.UserCFNotificationTypeAware} custom fields
     * @see UserCFNotificationTypeAware
     */
    public List getFields()
    {
        List fields = new ArrayList();

        Set fieldSet;
        try
        {
            fieldSet = fieldManager.getAllAvailableNavigableFields();
        }
        catch (FieldException e)
        {
            return Collections.EMPTY_LIST;
        }

        for (Iterator i = fieldSet.iterator(); i.hasNext();)
        {
            Field field = (Field) i.next();
            if (fieldManager.isCustomField(field))
            {
                CustomField customField = (CustomField) field;
                //Only add custom fields that implement UserCFNotificationTypeAware
                if (customField.getCustomFieldType() instanceof UserCFNotificationTypeAware)
                {
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    public boolean doValidation(String key, Map parameters)
    {
        String value = (String) parameters.get(key);
        if (value == null || value.length() == 0)
        {
            return false;
        }
        try
        {
            fieldManager.getCustomField(value);
        }
        catch (IllegalArgumentException ex)
        {
            return false;
        }
        return true;
    }

    public String getArgumentDisplay(String argument)
    {
        CustomField field = fieldManager.getCustomField(argument);
        return field.getName();
    }
}
