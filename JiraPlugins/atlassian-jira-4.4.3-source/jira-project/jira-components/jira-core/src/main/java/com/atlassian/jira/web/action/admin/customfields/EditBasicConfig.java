package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.issue.customfields.config.helper.BasicConfigDescriptor;
import com.atlassian.jira.issue.customfields.config.helper.BasicConfigFieldDescriptor;
import com.atlassian.jira.issue.customfields.config.helper.BasicConfigItemType;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.util.JiraUtils;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class EditBasicConfig extends AbstractEditConfigurationItemAction
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    private String className;
    private BasicConfigItemType configItemType;
    private BasicConfigDescriptor configDescriptor;

    // ---------------------------------------------------------------------------------------------------- Dependencies
    private final GenericConfigManager genericConfigManager;
    private Map values;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public EditBasicConfig(GenericConfigManager genericConfigManager)
    {
        this.genericConfigManager = genericConfigManager;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        try
        {
            init();

            return super.doDefault();
        }
        catch (Exception e)
        {
            addErrorMessage(getText("admin.errors.unable.to.instantiate.class") + " " + className);
            return ERROR;
        }
    }


    protected String doExecute() throws Exception
    {
        init();

        Map actionParams = ActionContext.getParameters();
        Map valuesToPersist = new HashMap();
        if (actionParams != null && configDescriptor.getConfigFields() != null)
        {
            for (Iterator iterator = configDescriptor.getConfigFields().iterator(); iterator.hasNext();)
            {
                BasicConfigFieldDescriptor descriptor = (BasicConfigFieldDescriptor) iterator.next();
                final String objectKey = descriptor.getKey();
                final String[] value = (String[]) actionParams.get(objectKey);
                valuesToPersist.put(objectKey, value != null && value.length > 0 ? value[0] : null);
            }
        }
        genericConfigManager.update(configItemType.getObjectKey(), getFieldConfigId().toString(), valuesToPersist);

        return getRedirect("ViewCustomFields.jspa");
    }

    // --------------------------------------------------------------------------------------------- View Helper Methods
    public String getFieldValue(String key)
    {
        if (values == null)
        {
            values = (Map) configItemType.getConfigurationObject(null, getFieldConfig());
        }

        if (values != null)
        {
            return (String) values.get(key);
        }
        else
        {
            return null;
        }
    }

    public String getTitle()
    {
        return configDescriptor.getTitle();
    }

    public String getInstructions()
    {
        return configDescriptor.getInstructions();
    }

    public List getConfigFields()
    {
        return configDescriptor.getConfigFields();
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    private void init()
            throws ClassNotFoundException
    {
        configItemType = (BasicConfigItemType) JiraUtils.loadComponent(className, EditBasicConfig.class);
        configDescriptor = configItemType.getBasicConfigDescriptor();
    }

    // -------------------------------------------------------------------------------------- Basic accessors & mutators
    public BasicConfigDescriptor getConfigDescriptor()
    {
        return configDescriptor;
    }

    public String getClassName()
    {
        return className;
    }

    public void setClassName(String className)
    {
        this.className = className;
    }
}
