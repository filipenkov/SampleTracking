package com.atlassian.jira.plugin.workflow;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.operation.IssueOperation;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.jira.web.action.issue.EditIssue;
import com.opensymphony.workflow.loader.AbstractDescriptor;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UpdateIssueFieldFunctionPluginFactory extends AbstractWorkflowPluginFactory implements WorkflowPluginFunctionFactory
{
    private static final String PARAM_NAME = "eventType";
    private final FieldManager fieldManager;
    private final List fields;
    public static final String PARAM_FIELD_ID = "fieldId";
    public static final String PARAM_FIELD_VALUE = "fieldValue";
    public static final String TARGET_FIELD_NAME = "field.name";
    public static final String TARGET_FIELD_VALUE = "field.value";

    public UpdateIssueFieldFunctionPluginFactory(FieldManager fieldManager)
    {
        this.fieldManager = fieldManager;
        this.fields = new ArrayList(9);

        //This list of fields is hardcoded on purpose.  We dont want users to be able to edit just
        //any field via this post function.
        this.fields.add(fieldManager.getField(IssueFieldConstants.ASSIGNEE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.DESCRIPTION));
        this.fields.add(fieldManager.getField(IssueFieldConstants.ENVIRONMENT));
        this.fields.add(fieldManager.getField(IssueFieldConstants.PRIORITY));
        this.fields.add(fieldManager.getField(IssueFieldConstants.RESOLUTION));
        this.fields.add(fieldManager.getField(IssueFieldConstants.SUMMARY));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_ORIGINAL_ESTIMATE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_ESTIMATE));
        this.fields.add(fieldManager.getField(IssueFieldConstants.TIME_SPENT));
    }


    protected void getVelocityParamsForInput(Map velocityParams)
    {
        velocityParams.put("fields", fields);
        velocityParams.put("factory", this);
    }

    protected void getVelocityParamsForEdit(Map velocityParams, AbstractDescriptor descriptor)
    {
        getVelocityParamsForInput(velocityParams);

        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        // Get the current arguments
        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;
        velocityParams.put(PARAM_FIELD_ID, functionDescriptor.getArgs().get(TARGET_FIELD_NAME));
        String value = (String) functionDescriptor.getArgs().get(TARGET_FIELD_VALUE);
        if (value == null || value.equals("null"))
        {
            velocityParams.put(PARAM_FIELD_VALUE, null);
        }
        else
        {
            velocityParams.put(PARAM_FIELD_VALUE, value);
        }
    }

    protected void getVelocityParamsForView(Map velocityParams, AbstractDescriptor descriptor)
    {
        if (!(descriptor instanceof FunctionDescriptor))
        {
            throw new IllegalArgumentException("Descriptor must be a FunctionDescriptor.");
        }

        FunctionDescriptor functionDescriptor = (FunctionDescriptor) descriptor;

        velocityParams.put(PARAM_FIELD_ID, fieldManager.getField((String) functionDescriptor.getArgs().get(TARGET_FIELD_NAME)).getNameKey());
        velocityParams.put(PARAM_FIELD_VALUE, functionDescriptor.getArgs().get(TARGET_FIELD_VALUE));
    }

    public Map getDescriptorParams(Map conditionParams)
    {
        Map params = new HashMap();

        String fieldId = extractSingleParam(conditionParams, PARAM_FIELD_ID);
        params.put(TARGET_FIELD_NAME, fieldId);

        String fieldValue = extractSingleParam(conditionParams, PARAM_FIELD_VALUE);

        params.put(TARGET_FIELD_VALUE, fieldValue);

        return params;
    }

    public String getEditHtml(final OrderableField field, final Object value)
    {
        OperationContext operationContext = new OperationContext()
        {
            public Map getFieldValuesHolder()
            {
                return EasyMap.build(field.getId(), value);
            }

            public IssueOperation getIssueOperation()
            {
                return new IssueOperation()
                {
                    // These methods are not used anywhere
                    public String getNameKey()
                    {
                        return "Populate Workflow Function";
                    }

                    public String getDescriptionKey()
                    {
                        return "Select a value that should be set on an issue when this function is executed.";
                    }
                };
            }
        };

        return field.getEditHtml(null,
                operationContext,
                (Action) JiraUtils.loadComponent(EditIssue.class),
                null,
                EasyMap.build(OrderableField.NO_HEADER_PARAM_KEY, "true", "displayNone", "true"));
    }

    // This method is called on different types of fields - not just Orderable fields
    public boolean hasCustomEditTemplate(Field field)
    {
        return IssueFieldConstants.PRIORITY.equals(field.getId()) ||
                IssueFieldConstants.ASSIGNEE.equals(field.getId()) ||
                IssueFieldConstants.RESOLUTION.equals(field.getId());
    }
}
