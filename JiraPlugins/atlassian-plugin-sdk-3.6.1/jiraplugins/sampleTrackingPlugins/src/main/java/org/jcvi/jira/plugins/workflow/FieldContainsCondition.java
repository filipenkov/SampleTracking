package org.jcvi.jira.plugins.workflow;

import com.atlassian.jira.plugin.workflow.WorkflowConditionModuleDescriptor;
import com.atlassian.jira.workflow.condition.AbstractJiraCondition;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.WorkflowException;

import java.util.Map;

/**
 *  This is the OSWorkflow level implementation.
 */
public class FieldContainsCondition extends AbstractJiraCondition {
    public static final String CONFIG_FIELD  = "field";
    public static final String CONFIG_VALUES = "values";
    public static final String CONFIG_INVERT = "invert";
    @Override
    public boolean passesCondition(Map transientVariables,
                                   Map args,
                                   PropertySet propertySet) throws WorkflowException {
        @SuppressWarnings("unchecked")
        JIRAWorkflowState<WorkflowConditionModuleDescriptor> state =
                new JIRAWorkflowState<WorkflowConditionModuleDescriptor>(
                        transientVariables,
                        args,
                        propertySet,
                        WorkflowConditionModuleDescriptor.class);

        String fieldToTest  = state.getArgument(CONFIG_FIELD);
        String valuesString = state.getArgument(CONFIG_VALUES);
        boolean invert      = state.getBooleanArgument(CONFIG_INVERT);
        final String[] values;
        if (valuesString != null && ! valuesString.trim().isEmpty()) {
            //note negative parameter to ensure an empty value at the end
            //of the string is returned.
            values = valuesString.split(",",-1);
        } else {
            values = null;
        }

        String fieldValue = state.getIssueFieldValue(fieldToTest);

        return checkValues(fieldValue,values) ^ invert;
    }

    private static boolean checkValues(String fieldValue, String[] values) {
        //no values to match, and so it can't be a match
        if (values == null || values.length == 0) {
            return false;
        }

        if (fieldValue == null) {
            fieldValue = ""; //replace with empty string to remove a special case
                             //from the compare loop
        } else {
            fieldValue = fieldValue.trim(); //only do this once
        }

        //compare the values
        for(String value : values) {
            if (fieldValue.equalsIgnoreCase(value.trim())) {
                return true;
            }
        }
        //nothing matched
        return false;

    }
}
