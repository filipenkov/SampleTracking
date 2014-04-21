package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.plugin.JiraResourcedModuleDescriptor;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import webwork.action.Action;

import java.util.Map;


public interface CustomFieldTypeModuleDescriptor extends JiraResourcedModuleDescriptor<CustomFieldType>
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

    // -------------------------------------------------------------------------------------------------- Public Methods
    public boolean isViewTemplateExists();

    public boolean isColumnViewTemplateExists();

    public boolean isEditTemplateExists();

    public boolean isXMLTemplateExists();

    // -------------------------------------------------------------------------------------------------- HTML Templates

    public String getEditHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    public String getBulkMoveHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
            Map displayParameters,
            FieldLayoutItem fieldLayoutItem, final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues, final BulkMoveHelper bulkMoveHelper);

    public String getEditDefaultHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public String getColumnViewHtml(CustomField field, Object value, Issue issue, Map displayParams, FieldLayoutItem fieldLayoutItem);

    //the value here is the customfield value
    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem);

    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem, Map displayParameters);

    public String getViewXML(CustomField field, Issue issue, FieldLayoutItem fieldLayoutItem, boolean raw);

    public String getDefaultViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem);
}
