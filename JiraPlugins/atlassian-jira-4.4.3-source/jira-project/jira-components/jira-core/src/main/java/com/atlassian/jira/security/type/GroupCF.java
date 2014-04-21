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
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.customfields.GroupSelectorField;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.util.GroupSelectorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Issue Security and Permission type for a Group Selector custom field, or select-list custom fields which specify groups.
 *
 * 
 *
 * @since 3.6
 */
public class GroupCF extends AbstractIssueFieldSecurityType
{
    private static final Logger log = Logger.getLogger(GroupCF.class);
    public static final String TYPE = "groupCF";

    private String desc = "groupCF";
    private JiraAuthenticationContext jiraAuthenticationContext;
    private GroupSelectorUtils groupSelectorUtils;
    private final CustomFieldManager customFieldManager;

    public GroupCF(JiraAuthenticationContext jiraAuthenticationContext, GroupSelectorUtils groupSelectorUtils, CustomFieldManager customFieldManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.groupSelectorUtils = groupSelectorUtils;
        this.customFieldManager = customFieldManager;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.permission.types.group.custom.field");
    }

    public String getType()
    {
        return TYPE;
    }

    @Override
    public Query getQuery(com.opensymphony.user.User searcher, GenericValue entity, String parameter)
    {
        desc = parameter; // TODO: dodgy
        return super.getQuery(searcher, entity, parameter);
    }

    @Override
    protected BooleanQuery getQueryForProject(GenericValue project, com.opensymphony.user.User searcher)
    {
        Collection groupNames = searcher.getGroups();
        if (groupNames != null && !groupNames.isEmpty())
        {
            BooleanQuery projectAndGroupQuery = new BooleanQuery();
            Query projectQuery = new TermQuery(new Term(DocumentConstants.PROJECT_ID, project.getString("id")));
            projectAndGroupQuery.add(projectQuery, BooleanClause.Occur.MUST);

            BooleanQuery groupQuery = getQueryForGroups(groupNames);
            projectAndGroupQuery.add(groupQuery, BooleanClause.Occur.MUST);
            return projectAndGroupQuery;
        }
        else
        {
            return null;
        }
    }

    private BooleanQuery getQueryForGroups(Collection groupNames)
    {
        BooleanQuery groupQuery = new BooleanQuery();
        for (Iterator iterator = groupNames.iterator(); iterator.hasNext();)
        {
            String groupName = (String) iterator.next();
            CustomField  customField = customFieldManager.getCustomFieldObject(desc);
            groupQuery.add(getQueryForGroupName(customField,groupName), BooleanClause.Occur.SHOULD);
        }
        return groupQuery;
    }

    private Query getQueryForGroupName(final CustomField customField, final String groupName)
    {
        Query termQuery = null;
        if (customField.getCustomFieldType() instanceof GroupSelectorField)
        {
            GroupSelectorField groupSelector = (GroupSelectorField) customField.getCustomFieldType();
            termQuery = groupSelector.getQueryForGroup(getFieldName(), groupName);
        } else {
            termQuery = new TermQuery(new Term(getFieldName(), groupName));
        }
        return termQuery;
    }

    @Override
    protected BooleanQuery getQueryForSecurityLevel(GenericValue issueSecurity, com.opensymphony.user.User searcher)
    {
        Collection groupNames = searcher.getGroups();
        if (groupNames != null && !groupNames.isEmpty())
        {
            BooleanQuery securityLevelAndGroupQuery = new BooleanQuery();
            Query securityLevelQuery = new TermQuery(new Term(DocumentConstants.ISSUE_SECURITY_LEVEL, issueSecurity.getString("id")));
            securityLevelAndGroupQuery.add(securityLevelQuery, BooleanClause.Occur.MUST);

            BooleanQuery groupQuery = getQueryForGroups(groupNames);
            securityLevelAndGroupQuery.add(groupQuery, BooleanClause.Occur.MUST);

            return securityLevelAndGroupQuery;
        }
        else
        {
            return null;
        }
    }

    public void doValidation(String key, Map parameters, JiraServiceContext jiraServiceContext)
    {
        //JRA-13808: Need to check whether or not the group CF has a searcher set.
        String customFieldOption = (String) parameters.get(getType());
        if (StringUtils.isEmpty(customFieldOption))
        {
            String localisedMessage = jiraServiceContext.getI18nBean().getText("admin.permissions.errors.please.select.group.customfield");
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
    {
        return desc;
    }

    @Override
    protected boolean hasProjectPermission(com.atlassian.crowd.embedded.api.User user, boolean issueCreation, GenericValue project)
    {
        return !issueCreation;
    }

    /**
     * Determines if the given user has permission to see the given issue by
     * using the custom field with the given custom field name to look up a
     * group. Returns true only if the user is in the group.
     * @param user the user for whom permission is being determined.
     * @param issueCreation not used.
     * @param issueGv the issue to which permission is being determined
     * @param customFieldName the name of the custom field.
     * @return true only if the user is in the group defined by the issue's named custom field.
     */
    @Override
    protected boolean hasIssuePermission(User user, boolean issueCreation, GenericValue issueGv, String customFieldName)
    {
        CustomFieldManager customFieldManager = ComponentAccessor.getCustomFieldManager();

        boolean hasPermission = false;

        CustomField field = customFieldManager.getCustomFieldObject(customFieldName);

        if (field == null)
        {
            log.warn("custom field '" + customFieldName + "' is missing, can't use it to determine permissions.");
        }
        else
        {
            IssueFactory issueFactory = (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class);
            hasPermission = groupSelectorUtils.isUserInCustomFieldGroup(issueFactory.getIssue(issueGv), field, user);
        }

        return hasPermission;

    }

    public List getDisplayFields()
    {
        return groupSelectorUtils.getCustomFieldsSpecifyingGroups();
    }

    @Override
    public String getArgumentDisplay(String argument)
    {
        FieldManager fieldManager = ComponentManager.getInstance().getFieldManager();
        if (fieldManager.isCustomField(argument))
        {
            CustomField field = fieldManager.getCustomField(argument);
            return field.getName();
        }
        else
        {
            return argument;
        }
    }

    /**
     * Get user specified by the Custom Field
     *
     * @param customFieldId eg. 'customfield_10000'
     */
    @Override
    public Set<com.opensymphony.user.User> getUsers(PermissionContext ctx, String customFieldId)
    {
        return groupSelectorUtils.getUsers(ctx.getIssue(), customFieldId);
    }
}
