/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.customfields;

import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebSudoRequired
public class ViewCustomFields extends JiraWebActionSupport
{
    private final CustomFieldManager customFieldManager;
    private final FieldScreenManager fieldScreenManager;
    private Map fieldScreenTabMap;

    public ViewCustomFields(CustomFieldManager customFieldManager, FieldScreenManager fieldScreenManager)
    {
        this.customFieldManager = customFieldManager;
        this.fieldScreenManager = fieldScreenManager;
        fieldScreenTabMap = new HashMap();
    }

    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    public String doReset() throws Exception
    {
        customFieldManager.refresh();
        return super.doDefault();
    }


    public List<CustomField> getCustomFields() throws Exception
    {
        return customFieldManager.getCustomFieldObjects();
    }

    public boolean isCustomFieldTypesExist()
    {
        Collection fieldTypes = customFieldManager.getCustomFieldTypes();
        return fieldTypes != null && !fieldTypes.isEmpty();
    }

    public boolean isHasConfigurableOptions(CustomField customField)
    {
        return customField.getCustomFieldType() instanceof MultipleSettableCustomFieldType;  
    }

    
    public Collection getFieldScreenTabs(OrderableField orderableField)
    {
        String fieldId = orderableField.getId();
        if (!fieldScreenTabMap.containsKey(fieldId))
        {
            fieldScreenTabMap.put(fieldId, fieldScreenManager.getFieldScreenTabs(orderableField.getId()));
        }

        return (Collection) fieldScreenTabMap.get(fieldId);
    }
}
