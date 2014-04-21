/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.List;
import java.util.Map;

public class GenerateChangeHistoryFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(GenerateChangeHistoryFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
        if (transientVars.get("changeItems") != null)
        {
            changeHolder.setChangeItems((List) transientVars.get("changeItems"));
        }

        // Ensure the updated date is set
        issue.setUpdated(UtilDateTime.nowTimestamp());

        // Store the issue
        issue.store();

        Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();
        if (!modifiedFields.isEmpty())
        {
            StringBuilder modifiedText = new StringBuilder();
            // Maybe move this code to the issue.store() method
            FieldManager fieldManager = ComponentManager.getInstance().getFieldManager();

            for (final String fieldId : modifiedFields.keySet())
            {
                if (fieldManager.isOrderableField(fieldId))
                {
                    OrderableField field = fieldManager.getOrderableField(fieldId);
                    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(field);
                    field.updateValue(fieldLayoutItem, issue, modifiedFields.get(fieldId), changeHolder);
                    if (IssueFieldConstants.DESCRIPTION.equals(fieldId) || IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                    {
                        modifiedText.append(modifiedFields.get(fieldId)).append(" ");
                    }
                }
            }
            // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
            // method of the issue, so that the fields removes itself from the modified list as soon as it is persisted.
            issue.resetModifiedFields();
        }

        if (!changeHolder.getChangeItems().isEmpty())
        {
            GenericValue changeGroup = ChangeLogUtils.createChangeGroup(WorkflowUtil.getCaller(transientVars), issue.getGenericValue(), issue.getGenericValue(), changeHolder.getChangeItems(), false);
            transientVars.put("changeGroup", changeGroup);
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", GenerateChangeHistoryFunction.class.getName());
        return descriptor;
    }
}
