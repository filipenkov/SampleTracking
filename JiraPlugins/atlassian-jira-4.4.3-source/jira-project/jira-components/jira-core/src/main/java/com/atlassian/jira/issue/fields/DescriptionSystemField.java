package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.DescriptionSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;

import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class DescriptionSystemField extends AbstractTextSystemField implements HideableField, RequirableField
{
    private static final String DESCRIPTION_NAME_KEY = "issue.field.description";
    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_SORT_DESC);
    private final RendererManager rendererManager;

    public DescriptionSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, RendererManager rendererManager, PermissionManager permissionManager, DescriptionSearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.DESCRIPTION, DESCRIPTION_NAME_KEY, velocityManager, applicationProperties, authenticationContext, rendererManager, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        if (fieldScreenRenderLayoutItem.isRequired())
        {
            String description = (String) fieldValuesHolder.get(getId());
            if (!TextUtils.stringSet(description) || description.trim().length() <= 0)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public String getValueFromIssue(Issue issue)
    {
        return issue.getDescription();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            // give the renderer a change to transform the incomming value
            String desc = (String)rendererManager.getRendererForType(rendererType).transformFromEdit(getValueFromParams(fieldValueHolder));
            if (TextUtils.stringSet(desc))
                issue.setDescription(desc);
            else
                issue.setDescription(null);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setDescription(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.description";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return SORTER;
    }

    protected String getEditTemplateName()
    {
        return "description-edit.vm";
    }

    protected String getColumnViewTemplateName()
    {
        return "description-columnview.vm";
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        String rendererType = null;

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (Iterator iterator = bulkEditBean.getFieldLayouts().iterator(); iterator.hasNext();)
        {
            FieldLayout fieldLayout = (FieldLayout) iterator.next();
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }

            // Check for different renderer type
            if (rendererType == null)
            {
                // rendererType not set yet - set it to rendererType for this Field Layout.
                rendererType = fieldLayout.getRendererTypeForField(IssueFieldConstants.DESCRIPTION);
            }
            else if (!rendererType.equals(fieldLayout.getRendererTypeForField(IssueFieldConstants.DESCRIPTION)))
            {
                // We have found two different Renderer Types.
                return "bulk.edit.unavailable.different.renderers";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailable message)
        return null;
    }
}
