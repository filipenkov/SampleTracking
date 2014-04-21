/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bulkedit.operation;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.google.common.collect.ImmutableList;
import org.apache.commons.collections.map.ListOrderedMap;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulkEditOperation extends AbstractBulkOperation
{
    public static final String NAME_KEY = "bulk.edit.operation.name";
    public static final String NAME = "BulkEdit";
    private static final String DESCRIPTION_KEY = "bulk.edit.operation.description";
    private static final String CANNOT_PERFORM_MESSAGE_KEY = "bulk.edit.cannotperform";
    private static final List<String> ALL_SYSTEM_FIELDS = ImmutableList.of(
            IssueFieldConstants.ISSUE_TYPE, IssueFieldConstants.SECURITY, IssueFieldConstants.PRIORITY,
            IssueFieldConstants.FIX_FOR_VERSIONS, IssueFieldConstants.AFFECTED_VERSIONS,
            IssueFieldConstants.COMPONENTS, IssueFieldConstants.ASSIGNEE, IssueFieldConstants.REPORTER,
            IssueFieldConstants.ENVIRONMENT, IssueFieldConstants.DUE_DATE, IssueFieldConstants.COMMENT,
            IssueFieldConstants.LABELS);
    private final IssueManager issueManager;
    private final PermissionManager permissionManager;
    private final ProjectManager projectManager;
    private final FieldManager fieldManager;
    private final JiraAuthenticationContext authenticationContext;

    public BulkEditOperation(IssueManager issueManager, PermissionManager permissionManager, ProjectManager projectManager, FieldManager fieldManager, JiraAuthenticationContext authenticationContext)
    {
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.projectManager = projectManager;
        this.fieldManager = fieldManager;
        this.authenticationContext = authenticationContext;
    }

    public boolean canPerform(final BulkEditBean bulkEditBean, final User remoteUser)
    {
        // Ensure that all selected issues can be edited
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (!issue.isEditable())
            {
                return false;
            }
        }

        // Check if any of the actions are available
        final Collection actions = getActions(bulkEditBean, remoteUser).values();
        for (Iterator iterator = actions.iterator(); iterator.hasNext();)
        {
            BulkEditAction bulkEditAction = (BulkEditAction) iterator.next();
            if (bulkEditAction.isAvailable(bulkEditBean))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Initialises all the bulk edit actions and returns them.
     *
     * @param bulkEditBean bean used for actions retrieval
     * @param remoteUser   remote user
     * @return bulk edit actions
     */
    public Map getActions(BulkEditBean bulkEditBean, User remoteUser)
    {
        Map actions = new ListOrderedMap();
        // Go through all system fields
        for (Iterator iterator = ALL_SYSTEM_FIELDS.iterator(); iterator.hasNext();)
        {
            String actionName = (String) iterator.next();
            actions.put(actionName, buildBulkEditAction(actionName));
        }

        // Add all custom field actions
        actions.putAll(getCustomFieldActions(bulkEditBean, remoteUser));

        return actions;
    }

    private BulkEditAction buildBulkEditAction(String fieldId)
    {
        return new BulkEditActionImpl(fieldId, fieldManager, authenticationContext);
    }

    public Map getCustomFieldActions(BulkEditBean bulkEditBean, User remoteUser)
    {
        Long projectId;
        if (!bulkEditBean.isMultipleProjects())
        {
            projectId = (Long) bulkEditBean.getProjectIds().iterator().next();
        }

        SearchContext searchContext = new SearchContextImpl(null, new ArrayList(bulkEditBean.getProjectIds()), new ArrayList(bulkEditBean.getIssueTypes()));

        List customFields = ComponentAccessor.getCustomFieldManager().getCustomFieldObjects(searchContext);
        List availableCustomFields = new ArrayList();
        for (int i = 0; i < customFields.size(); i++)
        {
            CustomField customField = (CustomField) customFields.get(i);
            // Need to check if the field is NOT hidden in ALL field layouts of selected projects
            for (Iterator iterator1 = bulkEditBean.getFieldLayouts().iterator(); iterator1.hasNext();)
            {
                FieldLayout fieldLayout = (FieldLayout) iterator1.next();
                if (!fieldLayout.isFieldHidden(customField.getId()))
                {
                    availableCustomFields.add(customField);
                }
            }
        }

        if (!availableCustomFields.isEmpty())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission in all projects of the selected issues
            for (Iterator iterator = bulkEditBean.getProjectIds().iterator(); iterator.hasNext();)
            {
                projectId = (Long) iterator.next();
                // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
                // just the ASSIGNEE permission, so the permissions to check depend on the field
                if (!hasPermission(Permissions.EDIT_ISSUE, projectManager.getProject(projectId), remoteUser))
                {
                    return EasyMap.build(null, new UnavailableBulkEditAction("common.concepts.customfields", "bulk.edit.unavailable.customfields", authenticationContext));
                }
            }

            Map bulkEditActions = new ListOrderedMap();
            // Create BulkEditActions to represent each bulk-editable custom field
            for (Iterator iterator = availableCustomFields.iterator(); iterator.hasNext();)
            {
                CustomField customField = (CustomField) iterator.next();
                bulkEditActions.put(customField.getId(), buildBulkEditAction(customField.getId()));
            }

            return bulkEditActions;
        }
        else
        {
            return EasyMap.build(null, new UnavailableBulkEditAction("common.concepts.customfields", "bulk.edit.unavailable.customfields", authenticationContext));
        }
    }

    private boolean hasPermission(int permission, GenericValue project, User remoteUser)
    {
        return permissionManager.hasPermission(permission, project, remoteUser);
    }

    public void perform(final BulkEditBean bulkEditBean, final User remoteUser) throws Exception
    {
        boolean sendMail = bulkEditBean.isSendBulkNotification();
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            MutableIssue issue = (MutableIssue) iterator.next();
            for (Iterator iterator1 = bulkEditBean.getActions().values().iterator(); iterator1.hasNext();)
            {
                BulkEditAction bulkEditAction = (BulkEditAction) iterator1.next();
                OrderableField field = bulkEditAction.getField();
                FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue.getProjectObject(), issue.getIssueTypeObject().getId()).getFieldLayoutItem(field);
                field.updateIssue(fieldLayoutItem, issue, bulkEditBean.getFieldValuesHolder());
            }

            issueManager.updateIssue(remoteUser, issue, EventDispatchOption.ISSUE_UPDATED, sendMail);
        }
    }

    public String getNameKey()
    {
        return NAME_KEY;
    }

    public String getDescriptionKey()
    {
        return DESCRIPTION_KEY;
    }

    public boolean equals(Object o)
    {
        return this == o || o instanceof BulkEditOperation;
    }

    public String getOperationName()
    {
        return NAME;
    }

    public String getCannotPerformMessageKey()
    {
        return CANNOT_PERFORM_MESSAGE_KEY;
    }
}
