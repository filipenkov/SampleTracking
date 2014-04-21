package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.List;

@WebSudoRequired
public class ConfigureCustomField extends JiraWebActionSupport
{
    private Long customFieldId;
    private final FieldConfigSchemeManager schemeManager;
    private final CustomFieldManager customFieldManager;

    public ConfigureCustomField(FieldConfigSchemeManager schemeManager, CustomFieldManager customFieldManager)
    {
        this.schemeManager = schemeManager;
        this.customFieldManager = customFieldManager;
    }

    protected String doExecute() throws Exception
    {
        return SUCCESS;
    }

    public List<FieldConfigScheme> getConfigs()
    {
        if (getCustomFieldId() != null)
        {
            CustomField customField = getCustomField();
            return schemeManager.getConfigSchemesForField(customField);
        }
        else
        {
            return null;
        }
    }

    public CustomField getCustomField()
    {
        return customFieldManager.getCustomFieldObject(getCustomFieldId());
    }

    public Long getCustomFieldId()
    {
        return customFieldId;
    }

    public void setCustomFieldId(Long customFieldId)
    {
        this.customFieldId = customFieldId;
    }
}
