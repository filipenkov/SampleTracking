package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.PrioritySearchHandlerFactory;
import com.atlassian.jira.issue.statistics.PriorityStatisticsMapper;
import com.atlassian.jira.issue.statistics.ReversePriorityStatisticsMapper;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.util.TextUtils;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class PrioritySystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField, IssueConstantsField
{
    private static final String PRIORITY_NAME_KEY = "issue.field.priority";

    private final ConstantsManager constantsManager;
    private final PriorityStatisticsMapper priorityStatisticsMapper;

    public PrioritySystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties,
                               ConstantsManager constantsManager, PermissionManager permissionManager,
                               JiraAuthenticationContext authenticationContext, PrioritySearchHandlerFactory handlerFactory)
    {
        super(IssueFieldConstants.PRIORITY, PRIORITY_NAME_KEY, velocityManager, applicationProperties, authenticationContext, permissionManager, handlerFactory);
        this.constantsManager = constantsManager;
        // Because we want priority to be 'ascending' in sort order for fields, but in stats we want it descending, we reverse it here.
        // Originally this was implemented as an anonymous inner class, but this leads to memory leaks as the compiled
        // class gets an implicit link to this PrioritySystemField instance.
        this.priorityStatisticsMapper = new ReversePriorityStatisticsMapper(constantsManager);
    }

    public String getCreateHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(FieldLayoutItem fieldLayoutItem, OperationContext operationContext, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParams.put(getId(), operationContext.getFieldValuesHolder().get(getId()));
        Collection priorityConstants = new ArrayList();
        Collection priorities = constantsManager.getPriorities();
        for (Iterator iterator = priorities.iterator(); iterator.hasNext();)
        {
            GenericValue priorityGV = (GenericValue) iterator.next();
            Priority priority = constantsManager.getPriorityObject(priorityGV.getString("id"));
            priorityConstants.add(priority);
        }
        velocityParams.put("priorities", priorityConstants);
        return renderTemplate("priority-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        // Create priority object
        Priority priority = issue.getPriorityObject();
        velocityParams.put("priority", priority);
        return getViewHtml(velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Object value, Map displayParameters)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, action, null, displayParameters);
        // Create priority object
        GenericValue priorityGV = (GenericValue) value;
        Priority priority = constantsManager.getPriorityObject(priorityGV.getString("id"));
        velocityParams.put("priority", priority);
        return getViewHtml(velocityParams);
    }

    private String getViewHtml(Map velocityParams)
    {
        return renderTemplate("priority-view.vm", velocityParams);
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String priorityId = (String) fieldValuesHolder.get(getId());

        // Check that if priority has been set to required that it is actually provided
        if (priorityId == null && fieldScreenRenderLayoutItem.isRequired())
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
        // Check that the issue type with the given id exists.
        else if (priorityId != null && getValueFromParams(fieldValuesHolder) == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.invalidpriority"));
        }
    }

    public Object getValueFromParams(Map params)
    {
        String priorityId = (String) params.get(getId());
        if (TextUtils.stringSet(priorityId))
            return constantsManager.getPriority(priorityId);
        else
            return null;
    }

    public void populateParamsFromString(Map fieldValuesHolder, String stringValue, Issue issue) throws FieldValidationException
    {
        Long priorityId = null;
        try
        {
            // Check if the issue type is a number
            priorityId = Long.valueOf(stringValue);
        }
        catch (NumberFormatException e)
        {
            // If not, try to convert to a number
            priorityId = getPriorityTypeIdByName(stringValue);
        }

        // Yes, priority id is a String, even though it is actually a number.
        // Ahh, the joy of backwards compatibility
        fieldValuesHolder.put(getId(), priorityId.toString());
    }

    private Long getPriorityTypeIdByName(String stringValue) throws FieldValidationException
    {
        for (Iterator iterator = constantsManager.getPriorities().iterator(); iterator.hasNext();)
        {
            GenericValue priorityGV = (GenericValue) iterator.next();
            if (stringValue.equalsIgnoreCase(priorityGV.getString("name")))
            {
                return Long.valueOf(priorityGV.getString("id"));
            }
        }

        throw new FieldValidationException("Invalid priority name '" + stringValue + "'.");
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
                GenericValue priority = (GenericValue) value;
                cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), null, null, priority.getString("id"), priority.getString("name"));
            }
        }
        else
        {
            if (!valuesEqual(value, currentValue))
            {
                GenericValue currentPriority = (GenericValue) currentValue;
                if (value != null)
                {
                    GenericValue priority = (GenericValue) value;
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentPriority.getString("id"), currentPriority.getString("name"), priority.getString("id"), priority.getString("name"));
                }
                else
                {
                    cib = new ChangeItemBean(ChangeItemBean.STATIC_FIELD, getId(), currentPriority.getString("id"), currentPriority.getString("name"), null, null);
                }
            }
        }

        if (cib != null) {
            issueChangeHolder.addChangeItem(cib);
        }
    }

    public void createValue(Issue issue, Object value)
    {
        // The field is recorded on the issue itself so there is nothing to do
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

    public void populateFromIssue(Map fieldValuesHolder, Issue issue)
    {
        GenericValue priority = issue.getPriority();
        if (priority != null)
            fieldValuesHolder.put(getId(), priority.getString("id"));
        else
            fieldValuesHolder.put(getId(), null);
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(Map fieldValuesHolder, Issue issue)
    {
        GenericValue defaultPriority = (GenericValue) getDefaultValue(issue);
        if (defaultPriority != null)
            fieldValuesHolder.put(getId(), defaultPriority.getString("id"));
        else
            fieldValuesHolder.put(getId(), null);
    }

    public Object getDefaultValue(Issue issue)
    {
        return getDefaultPriority();
    }

    public GenericValue getDefaultPriority()
    {
        return constantsManager.getDefaultPriority();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            issue.setPriority((GenericValue) getValueFromParams(fieldValueHolder));
        }
    }

    public MessagedResult needsMove(Collection originalIssues, Issue targetIssue, FieldLayoutItem targetFieldLayoutItem)
    {
        for (Iterator iterator = originalIssues.iterator(); iterator.hasNext();)
        {
            Issue originalIssue = (Issue) iterator.next();
            if (originalIssue.getPriority() == null && targetFieldLayoutItem.isRequired())
                return new MessagedResult(true);
        }
        return new MessagedResult(false);
    }

    public void populateForMove(Map fieldValuesHolder, Issue originalIssue, Issue targetIssue)
    {
        // If the field need to be moved then it does not have a current value, so populate the default
        populateDefaults(fieldValuesHolder, targetIssue);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setPriority(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public boolean hasValue(Issue issue)
    {
        return (issue.getPriority() != null);
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (Iterator iterator = bulkEditBean.getFieldLayouts().iterator(); iterator.hasNext();)
        {
            FieldLayout fieldLayout = (FieldLayout) iterator.next();
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permission for all issues (need to loop through all issues in case the permission has been given to
        // current assignee/reporter - i.e. to a role)
        for (Iterator iterator = bulkEditBean.getSelectedIssues().iterator(); iterator.hasNext();)
        {
            Issue issue = (Issue) iterator.next();
            // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
            // just the ASSIGNEE permission, so the permissions to check depend on the field
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    //////////////////////////////////////// NavigableField inmplementation ///////////////////////////////////////////
    public String getColumnHeadingKey()
    {
        return "issue.column.heading.priority";
    }

    public String getDefaultSortOrder()
    {
        return ORDER_DESCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return priorityStatisticsMapper;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put(getId(), issue.getPriorityObject());
        return renderTemplate("priority-columnview.vm", velocityParams);
    }

    public Collection getIssueConstants()
    {
        return constantsManager.getPriorityObjects();
    }

    /**
     * Return an internationalized value for the changeHistory item - a priority name in this case.
     *
     * @param changeHistory     name of priority
     * @param i18nHelper        used to translate the priority name
     * @return String
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper)
    {
        if (TextUtils.stringSet(changeHistory))
        {
            Long priorityId = getPriorityTypeIdByName(changeHistory);

            if (priorityId != null)
            {
                Priority priority = constantsManager.getPriorityObject(priorityId.toString());
                if (priority != null)
                {
                    return priority.getNameTranslation(i18nHelper);
                }
            }
        }
        // Otherwise return the original string
        return changeHistory;
    }
}
