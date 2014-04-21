package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.plugin.module.ModuleFactory;
import webwork.action.Action;

import java.util.Map;


public abstract class CustomFieldTypeModuleDescriptor extends JiraResourcedModuleDescriptor<CustomFieldType>
{
    // ------------------------------------------------------------------------------------------------------- Constants
    public static final String TEMPLATE_NAME_VIEW = "view";
    public static final String TEMPLATE_NAME_EDIT = "edit";
    public static final String TEMPLATE_NAME_EDIT_DEFAULT = "edit-default";
    public static final String TEMPLATE_NAME_BULK_MOVE = "bulk-move";
    public static final String TEMPLATE_NAME_XML = "xml";
    public static final String TEMPLATE_NAME_COLUMN = "column-view";

    public static final String VELOCITY_VALUE_PARAM = "value";
    public static final String VELCITY_ACTION_PARAM = "action";

    // ---------------------------------------------------------------------------------------------------- Constructors
    public CustomFieldTypeModuleDescriptor(JiraAuthenticationContext authenticationContext, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public abstract boolean isViewTemplateExists();

    public abstract boolean isColumnViewTemplateExists();

    public abstract boolean isEditTemplateExists();

    public abstract boolean isXMLTemplateExists();

    // -------------------------------------------------------------------------------------------------- HTML Templates

    public abstract String getEditHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    public abstract String getBulkMoveHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
            Map displayParameters,
            FieldLayoutItem fieldLayoutItem, final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues, final BulkMoveHelper bulkMoveHelper);

    public abstract String getEditDefaultHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public abstract String getColumnViewHtml(CustomField field, Object value, Issue issue, Map displayParams, FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public abstract String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem);

    public abstract String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem, Map displayParameters);

    public abstract String getViewXML(CustomField field, Issue issue, FieldLayoutItem fieldLayoutItem, boolean raw);

    public abstract String getDefaultViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem);
}
