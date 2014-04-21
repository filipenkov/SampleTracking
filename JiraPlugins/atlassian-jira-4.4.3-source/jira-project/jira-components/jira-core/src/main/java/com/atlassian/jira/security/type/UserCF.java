/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.customfields.impl.MultiUserCFType;
import com.atlassian.jira.issue.customfields.impl.UserCFType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.search.Query;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Issue Security and Permission type for a User Selector custom field,
 * or select-list custom fields which specify users.
 */
public class UserCF extends AbstractIssueFieldSecurityType
{
    public static final String TYPE = "userCF";
    private static final Logger log = Logger.getLogger(UserCF.class);

    private String desc = "userCF";
    private JiraAuthenticationContext jiraAuthenticationContext;
    private final CustomFieldManager customFieldManager;

    public UserCF(JiraAuthenticationContext jiraAuthenticationContext, CustomFieldManager customFieldManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.customFieldManager = customFieldManager;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.user.custom.field");
    }

    public String getType()
    {
        return TYPE;
    }

    @Override
    public Query getQuery(com.opensymphony.user.User searcher, GenericValue entity, String parameter)
    {
        desc = parameter; // TODO this is dodgy
        return super.getQuery(searcher, entity, parameter);
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        //JRA-13808: Need to check whether or not the user CF has a searcher set.
        String customFieldOption = (String) parameters.get(getType());
        if (StringUtils.isEmpty(customFieldOption))
        {
            String localisedMessage = jiraServiceContext.getI18nBean().getText("admin.permissions.errors.please.select.user.customfield");
            jiraServiceContext.getErrorCollection().addErrorMessage(localisedMessage);
        }
        else
        {
            // passed in parameters names a Custom Field - lets investigate.
            CustomField customField = customFieldManager.getCustomFieldObject(customFieldOption);
            if (customField != null && customField.getCustomFieldSearcher() == null)
            {
                // In order to use a Custom Field it must be indexed in Lucene Index. Currently we only index custom fields if they have a Searcher.
                // Message: "Custom field '{0}' is not indexed for searching - please add a searcher to this Custom Field."
                String localisedMessage = jiraServiceContext.getI18nBean().getText("admin.permissions.errors.customfieldnotindexed", customField.getName());
                jiraServiceContext.getErrorCollection().addErrorMessage(localisedMessage);
            }
        }
    }

    @Override
    protected String getFieldName()
    {   //return custom field id
        return desc;
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return !issueCreation;
    }

    /**
     * Defines whether the given user has permission to see the given issue using
     * the issue's value of the named custom field.
     *
     * @param user            the user whose permission is being checked.
     * @param issueCreation   unused.
     * @param issueGv         a GenericValue representing the issue.
     * @param customFieldName user custom field to use to determine which users have permission to the issue.
     * @return true only if the user has permission to see the issue based on the custom field value.
     */
    @Override
    protected boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String customFieldName)
    {
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();
        IssueFactory issueFactory = ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        CustomField field = customFieldManager.getCustomFieldObject(customFieldName);

        if (field != null)
        {
            Set values = convertToValueSet(getValuesFromIssue(field, issueFactory.getIssue(issueGv)));
            return values.contains(user);
        }
        return false;
    }

    public List getDisplayFields()
    {
        List fields = new ArrayList();

        FieldManager fieldManager = getFieldManager();
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
                if (customField.getCustomFieldType() instanceof UserCFType || customField.getCustomFieldType() instanceof MultiUserCFType)
                {
                    fields.add(field);
                }
            }
        }

        return fields;
    }

    @Override
    public String getArgumentDisplay(String argument)
    {
        CustomFieldManager fieldManager = ComponentAccessor.getCustomFieldManager();
        CustomField field = fieldManager.getCustomFieldObject(argument);
        return (field != null) ? field.getName() : argument;
    }

    /**
     * Get user specified by the Custom Field
     *
     * @param ctx           permission context
     * @param customFieldId eg. 'customfield_10000'
     * @return the set of users stored in the given custom field in the given context; an empty set is returned if the
     * context could not provide values (i.e. there was no issue specified).
     */
    @Override
    public Set<com.opensymphony.user.User> getUsers(PermissionContext ctx, String customFieldId)
    {
        // JRA-15063: just return EMPTY SET (like GroupCF does) because throwing an IllegalArgumentException is unnecessary.
        if (ctx.getIssue() == null)
        {
            log.info("Could not retrieve users for UserCF '" + customFieldId + "' since provided PermissionContext has no issue.");
            return Collections.EMPTY_SET;
        }
        Issue issue = ctx.getIssue();
        FieldManager fieldManager = getFieldManager();
        CustomField field = fieldManager.getCustomField(customFieldId);
        return convertToValueSet(getValuesFromIssue(field, issue));
    }

    /**
     * @param field the custom field to get the values of
     * @param issue the issue to get the value from
     * @return either a single user value or a {@link java.util.Collection} of values.
     */
    Object getValuesFromIssue(final CustomField field, final Issue issue)
    {
        return field.getCustomFieldType().getValueFromIssue(field, issue);
    }

    FieldManager getFieldManager()
    {
        return ComponentManager.getInstance().getFieldManager();
    }

    /**
     * Converts the given field values object into a set of values.
     *
     * @param fieldValues field values
     * @return a set of field values
     */
    Set convertToValueSet(Object fieldValues)
    {
        Set users = new HashSet(1);

        if (fieldValues != null)
        {
            if (fieldValues instanceof Collection) // http://jira.atlassian.com/browse/JRA-13523
            {
                users.addAll((Collection) fieldValues);
            }
            else
            {
                users.add(fieldValues);
            }
        }
        return users;
    }
}
