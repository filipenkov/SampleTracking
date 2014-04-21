package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.SummarySearchHandlerFactory;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class SummarySystemField extends AbstractTextSystemField implements NavigableField, MandatoryField
{
    private static final String SUMMARY_NAME_KEY = "issue.field.summary";
    public static final Long MAX_LEN = 255L;
    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_SORT_SUMMARY);

    public SummarySystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            RendererManager rendererManager, PermissionManager permissionManager, SummarySearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.SUMMARY, SUMMARY_NAME_KEY, velocityManager, applicationProperties, authenticationContext, rendererManager, permissionManager, searchHandlerFactory);
    }

    protected String getEditTemplateName()
    {
        return "summary-edit.vm";
    }

    protected String getColumnViewTemplateName()
    {
        return "summary-columnview.vm";
    }

    protected void populateVelocityParams(Map fieldValuesHolder, Map params)
    {
        super.populateVelocityParams(fieldValuesHolder, params);
        params.put("maxLen", MAX_LEN);
    }

    protected void populateVelocityParams(FieldLayoutItem fieldLayoutItem, Issue issue, Map params)
    {
        super.populateVelocityParams(fieldLayoutItem, issue, params);
        if (issue.isSubTask())
        {
            params.put("subTask", Boolean.TRUE);
            Issue parentIssue = issue.getParentObject();
            // do they have permission to see the parent issue, if not just show the key and not the summary
            params.put("parentIssueKey", parentIssue.getKey());
            params.put("subTaskParentIssueLinkDisabled", Boolean.TRUE);
            if (getPermissionManager().hasPermission(Permissions.BROWSE, parentIssue, getAuthenticationContext().getUser()))
            {
                params.put("parentIssueSummary", parentIssue.getSummary());
                params.put("subTaskParentIssueLinkDisabled", Boolean.FALSE);
            }
        }
    }

    public String getValueFromIssue(Issue issue)
    {
        return issue.getSummary();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setSummary((String) getValueFromParams(fieldValueHolder));
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setSummary(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getSummary());
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    /**
     * validate the field value
     *
     * @param operationContext            OperationContext
     * @param errorCollectionToAddTo      ErrorCollection
     * @param i18n                        I18nHelper
     * @param issue                       Issue
     * @param fieldScreenRenderLayoutItem FieldScreenRenderLayoutItem
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String summary = (String) getValueFromParams(fieldValuesHolder);
        //JRADEV-1867 User can cretae summary with "  "
        if (null != summary) {
            summary=summary.trim();
        }
        if (!TextUtils.stringSet(summary))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.specify.a.summary"));
        }
        else if (summary.length() > MAX_LEN.longValue())
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.summary.less.than", MAX_LEN.toString()));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.summary";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return SORTER;
    }

    public boolean isRenderable()
    {
        return false;
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }
}
