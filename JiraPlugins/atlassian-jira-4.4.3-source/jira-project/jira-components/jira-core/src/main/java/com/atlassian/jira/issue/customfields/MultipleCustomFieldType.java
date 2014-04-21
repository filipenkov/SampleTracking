package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;

public interface MultipleCustomFieldType extends CustomFieldType
{
    public Options getOptions(FieldConfig fieldConfig, JiraContextNode jiraContextNode);

}
