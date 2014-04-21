package com.atlassian.jira.plugin.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.plugin.workflow.JiraWorkflowPluginConstants;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.BulkMoveHelper;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import org.dom4j.Element;
import webwork.action.Action;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


public class CustomFieldTypeModuleDescriptorImpl extends CustomFieldTypeModuleDescriptor
{
    // ------------------------------------------------------------------------------------------------- Type Properties
    private RendererManager rendererManager;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public CustomFieldTypeModuleDescriptorImpl(JiraAuthenticationContext authenticationContext, RendererManager rendererManager, final ModuleFactory moduleFactory)
    {
        super(authenticationContext, moduleFactory);
        this.rendererManager = rendererManager;
    }

    // -------------------------------------------------------------------------------------------------- Public Methods
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
    }

    public void enabled()
    {
        super.enabled();
        assertModuleClassImplements(CustomFieldType.class);
    }

    public boolean isViewTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_VIEW);
    }

    public boolean isColumnViewTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_COLUMN);
    }

    public boolean isEditTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_EDIT);
    }

    public boolean isXMLTemplateExists()
    {
        return isResourceExist(TEMPLATE_NAME_XML);
    }

    // -------------------------------------------------------------------------------------------------- HTML Templates

    public String getEditHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem)
    {
        notNull("config", config);

        Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                                                  displayParameters);

        return getHtml(TEMPLATE_NAME_EDIT, params);
    }

    public String getBulkMoveHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
            Map displayParameters,
            FieldLayoutItem fieldLayoutItem, final Map<Long, BulkMoveHelper.DistinctValueResult> distinctValues, final BulkMoveHelper bulkMoveHelper)
    {
        notNull("config", config);

        final Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                                                  displayParameters);

        params.put("valuesToMap", distinctValues);
        params.put("bulkMoveHelper", bulkMoveHelper);

        String html;

        if (getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, TEMPLATE_NAME_BULK_MOVE) != null)
        {
            html = getHtml(TEMPLATE_NAME_BULK_MOVE, params);
        }
        else
        {
            html = getHtml(TEMPLATE_NAME_EDIT, params);
        }

        return html;
    }

    public String getEditDefaultHtml(FieldConfig config, Map customFieldValuesHolder, Issue issue, Action action,
                              Map displayParameters,
                              FieldLayoutItem fieldLayoutItem)
    {
        notNull("config", config);

        Map<String, Object> params = CustomFieldUtils.buildParams(config.getCustomField(), config, issue, fieldLayoutItem, null, customFieldValuesHolder, action,
                                                  displayParameters);

        String html;

        if (getResourceDescriptor(JiraWorkflowPluginConstants.RESOURCE_TYPE_VELOCITY, TEMPLATE_NAME_EDIT_DEFAULT) != null)
        {
            html = getHtml(TEMPLATE_NAME_EDIT_DEFAULT, params);
        }
        else
        {
            html = getHtml(TEMPLATE_NAME_EDIT, params);
        }

        return html;
    }


    //the value here is the customfield value
    public String getColumnViewHtml(CustomField field, Object value, Issue issue, Map displayParams, FieldLayoutItem fieldLayoutItem)
    {
        if (isResourceExist(TEMPLATE_NAME_COLUMN))
        {
            return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_COLUMN, displayParams);
        }
        else
        {
            return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_VIEW, displayParams);
        }
    }


    //the value here is the customfield value
    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem)
    {
        return getViewHtml(field, value, issue, fieldLayoutItem, null);
    }

    public String getViewHtml(CustomField field, Object value, Issue issue, FieldLayoutItem fieldLayoutItem, Map displayParameters)
    {
        return getViewHtmlByValue(fieldLayoutItem, value, field, issue, TEMPLATE_NAME_VIEW, displayParameters);
    }

    public String getViewXML(CustomField field, Issue issue, FieldLayoutItem fieldLayoutItem, boolean raw)
    {
        final Map<String, Object> combinedMap = getCombinedMap(field.getCustomFieldType().getVelocityParameters(issue, field, fieldLayoutItem), MapBuilder.build(VELOCITY_VALUE_PARAM, field.getValue(issue)));
        if(field.isRenderable() && !raw)
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            combinedMap.put("renderedValue", rendererManager.getRenderedContent(rendererType, (String)field.getValue(issue), issue.getIssueRenderContext()));
        }
        return getHtml(TEMPLATE_NAME_XML, combinedMap);
    }

    public String getDefaultViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        Object value = fieldConfig.getCustomField().getCustomFieldType().getDefaultValue(fieldConfig);
        return getViewHtmlByValue(fieldLayoutItem, value, fieldConfig.getCustomField(), null, TEMPLATE_NAME_VIEW, null);
    }


    // -------------------------------------------------------------------------------------------------- Helper Methods
    private String getViewHtmlByValue(FieldLayoutItem fieldLayoutItem, Object value, CustomField field, Issue issue, String templateNameView, Map displayParams)
    {
        Map<String, Object> params = CustomFieldUtils.buildParams(field,
                                                  null, // @TODO we could infer this (field.getRelevantConfig(issue)) but it's not very efficient
                                                  issue,
                                                  fieldLayoutItem,
                                                  value,
                                                  null, // no customFieldsValueHolder
                                                  null, // no action passed, again, we could make this passed down
                                                  displayParams
        );

        return getHtml(templateNameView, params);
    }

    private Map<String, Object> getCombinedMap(Map<String, Object> map1, Map<String, Object> map2)
    {
        Map<String, Object> allParams = new HashMap<String, Object>();
        if (map1 != null)
        {
            allParams.putAll(map1);
        }
        if (map2 != null)
        {
            allParams.putAll(map2);
        }
        return allParams;
    }

}
