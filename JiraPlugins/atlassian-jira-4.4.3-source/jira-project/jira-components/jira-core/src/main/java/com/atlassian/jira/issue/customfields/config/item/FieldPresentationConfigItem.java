package com.atlassian.jira.issue.customfields.config.item;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.config.helper.BasicConfigDescriptor;
import com.atlassian.jira.issue.customfields.config.helper.BasicConfigItemType;
import com.atlassian.jira.issue.customfields.config.helper.StylesConfigDescriptor;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.util.JiraUtils;

import java.util.Map;

public class FieldPresentationConfigItem implements BasicConfigItemType
{
    private final GenericConfigManager genericConfigManager;

    public static final String OBJECT_KEY = "styles";


    public FieldPresentationConfigItem(GenericConfigManager genericConfigManager)
    {
        this.genericConfigManager = genericConfigManager;
    }


    public String getDisplayName()
    {
        return "Field Presentation";
    }

    public String getDisplayNameKey()
    {
        return "admin.issuefields.customfields.config.field.presentation";
    }

    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        Map styles = (Map) getConfigurationObject(null, fieldConfig);
        if (styles != null)
        {
            return styles.toString();
        }
        else
        {
            return "Using default styles";
        }
    }

    public String getObjectKey()
    {
        return OBJECT_KEY;
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        return genericConfigManager.retrieve(getObjectKey(), config.getId().toString());
    }

    public String getBaseEditUrl()
    {
        return "EditBasicConfig!default.jspa?className=" + FieldPresentationConfigItem.class.getName();
    }

    public BasicConfigDescriptor getBasicConfigDescriptor()
    {
        return (BasicConfigDescriptor) JiraUtils.loadComponent(StylesConfigDescriptor.class);
    }
}
