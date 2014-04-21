package com.atlassian.jira.issue.fields;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.ReporterSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.ReporterStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.OSUserConverter;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.plugin.webresource.WebResourceManager;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;
import webwork.action.Action;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class ReporterSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField, UserField
{
    private static final Logger log = Logger.getLogger(ReporterSystemField.class);
    private static final String REPORTER_NAME_KEY = "issue.field.reporter";

    private final ReporterStatisticsMapper reporterStatisticsMapper;
    private UserPickerSearchService searchService;
    private ApplicationProperties applicationProperties;

    public ReporterSystemField(VelocityManager velocityManager, PermissionManager permissionManager,
                               ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
                               ReporterStatisticsMapper reporterStatisticsMapper, UserPickerSearchService searchService,
                               ReporterSearchHandlerFactory reporterSearchHandlerFactory)
    {
        super(IssueFieldConstants.REPORTER, REPORTER_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, reporterSearchHandlerFactory);
        this.reporterStatisticsMapper = reporterStatisticsMapper;
        this.searchService = searchService;
        this.applicationProperties = applicationProperties;
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        velocityParams.put("hasPermissionPickUsers", Boolean.valueOf(getPermissionManager().hasPermission(Permissions.USER_PICKER, getAuthenticationContext().getUser())));

        JiraServiceContext ctx = new JiraServiceContextImpl(authenticationContext.getUser());

        boolean canPerformAjaxSearch = searchService.canPerformAjaxSearch(ctx);
        if (canPerformAjaxSearch)
        {
            velocityParams.put("canPerformAjaxSearch", "true");
            velocityParams.put("ajaxLimit", applicationProperties.getDefaultBackedString(APKeys.JIRA_AJAX_AUTOCOMPLETE_LIMIT));
        }
        WebResourceManager webResourceManager = ComponentManager.getInstance().getWebResourceManager();
        webResourceManager.requireResource("jira.webresources:autocomplete");
        return renderTemplate("reporter-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put("reporter", issue.getReporter());
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        velocityParams.put("reporter", value);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("reporter-view.vm", velocityParams);
    }

    public boolean isShown(Issue issue)
    {
        return hasPermission(issue, Permissions.MODIFY_REPORTER);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        // If the value has been entered into the parameters the user must have permission to edit the reporter
        String returnedReporter = (String) fieldValuesHolder.get(getId());
        if (TextUtils.stringSet(returnedReporter))
        {
            // If the username has actually been given then check that the user actually exists
            // Should we check that the user has CREATE ISSUE permission in the project?
            if (!UserUtils.userExists(returnedReporter))
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.reporter.does.not.exist"));
                return;
            }
        }

        if (fieldScreenRenderLayoutItem.isRequired() && !TextUtils.stringSet(returnedReporter))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        // The default value that should be set if the user cannot modify this field is the remote user's name
        return getAuthenticationContext().getUser();
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            User reporter = (User) getValueFromParams(fieldValueHolder);
            issue.setReporter(reporter);
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Iterator iterator = originalIssues.iterator(); iterator.hasNext();)
        {
            Issue originalIssue = (Issue) iterator.next();

            // If the reporter of the original issue does not have permission to create issues in the target project then the reporter needs to be updated
            if (getPermissionManager().hasPermission(Permissions.CREATE_ISSUE, targetIssue.getProjectObject(), originalIssue.getReporter()))
            {
                // Otherwise need to be updated if the current reporter is not set and the target project has reporter as a required field
                if (originalIssue.getReporter() == null && targetFieldLayoutItem.isRequired())
                    return new MessagedResult(true);
            }
            else
            {
                return new MessagedResult(true);
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // Default to the reporter of the original issue  - JRA-7152
        User originalReporter = originalIssue.getReporterUser();
        if (originalReporter != null)
            fieldValuesHolder.put(getId(), originalReporter.getName());
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setReporter(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getReporter() != null);
    }

    public void updateValue(FieldLayoutItem fieldLayoutItem, Issue issue, ModifiedValue modifiedValue, IssueChangeHolder issueChangeHolder)
    {
        Object currentValue = modifiedValue.getOldValue();
        Object value = modifiedValue.getNewValue();
        ChangeItemBean cib = null;

        if (currentValue == null)
        {
            if (value != null)
            {
                User reporter = (User) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, reporter.getName(), reporter.getDisplayName());
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                User currentReporter = (User) currentValue;
                if (value != null)
                {
                    User reporter = (User) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentReporter.getName(), currentReporter.getDisplayName(), reporter.getName(), reporter.getDisplayName());
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentReporter.getName(), currentReporter.getDisplayName(), null, null);
                }
            }
        }

        if (cib != null)
            issueChangeHolder.addChangeItem(cib);
    }

    public Object getValueFromParams(Map params)
    {
        String username = (String) params.get(getId());

        if (TextUtils.stringSet(username))
        {
            // Retrieve the selected user
            final User user = UserUtils.getUser(username);
            // Some code relies on this being the old User - eg see TestBulkWorkflowTransition
            return OSUserConverter.convertToOSUser(user);
        }
        else
        {
            // If no username has been given resort to no reporter
            return null;
        }
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        fieldValuesHolder.put(getId(), stringValue);
    }

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        final User reporter = issue.getReporterUser();
        if (reporter != null)
        {
            fieldValuesHolder.put(getId(), reporter.getName());
        }
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        User remoteUser = (User) getDefaultValue(issue);
        if (remoteUser != null)
            fieldValuesHolder.put(getId(), remoteUser.getName());
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }


    public String getColumnHeadingKey()
    {
        return "issue.column.heading.reporter";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return reporterStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        final Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        try
        {
            final String reporterUserId = issue.getReporterId();
            if (reporterUserId != null)
            {
                velocityParams.put("reporterUsername", reporterUserId);
            }
        }
        catch (DataAccessException e)
        {
            log.debug("Error occurred retrieving reporter", e);
        }
        return renderTemplate("reporter-columnview.vm", velocityParams);
    }

    protected Object getRelevantParams(Map params)
    {
        String[] value = (String[]) params.get(getId());
        if (value != null && value.length > 0)
        {
            return value[0];
        }
        else
        {
            return null;
        }
    }
}
