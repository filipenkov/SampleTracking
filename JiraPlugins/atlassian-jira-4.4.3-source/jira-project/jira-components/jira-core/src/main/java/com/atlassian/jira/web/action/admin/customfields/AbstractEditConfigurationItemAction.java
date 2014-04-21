package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;

public abstract class AbstractEditConfigurationItemAction extends JiraWebActionSupport
{
    private Long fieldConfigId;
    private FieldConfig fieldConfig;

    public void setFieldConfigId(Long fieldConfigId)
    {
        this.fieldConfigId = fieldConfigId;
    }

    public Long getFieldConfigId()
    {
        return fieldConfigId;
    }

    public FieldConfig getFieldConfig()
    {
        if (fieldConfig == null && fieldConfigId != null)
        {
            final FieldConfigManager fieldConfigManager = ComponentManager.getComponent(FieldConfigManager.class);
            fieldConfig = fieldConfigManager.getFieldConfig(fieldConfigId);
        }

        return fieldConfig;
    }

    public CustomField getCustomField()
    {
        return getFieldConfig().getCustomField();
    }
}
