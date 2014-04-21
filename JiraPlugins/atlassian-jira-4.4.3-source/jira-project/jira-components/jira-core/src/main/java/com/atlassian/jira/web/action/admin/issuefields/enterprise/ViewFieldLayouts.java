package com.atlassian.jira.web.action.admin.issuefields.enterprise;

import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayoutImpl;
import com.atlassian.jira.issue.fields.layout.field.FieldConfigurationScheme;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class ViewFieldLayouts extends AbstractFieldLayoutAction
{
    private String confirm;
    private Map<Long, Collection<FieldConfigurationScheme>> fieldLayoutSchemeMap;

    public ViewFieldLayouts(FieldLayoutManager fieldLayoutManager)
    {
        super(fieldLayoutManager);
        fieldLayoutSchemeMap = new HashMap<Long, Collection<FieldConfigurationScheme>>();
    }

    protected String doExecute() throws Exception
    {
        return getResult();
    }

    @RequiresXsrfCheck
    public String doAddFieldLayout()
    {
        validateName();

        if (!invalidInput())
        {
            // Ensure no field layout with this name exists
            for (final FieldLayout fieldLayout : getFieldLayouts())
            {
                if (getFieldLayoutName().equals(fieldLayout.getName()))
                {
                    addError("fieldLayoutName", getText("admin.errors.fieldlayout.name.exists"));
                    break;
                }
            }
        }

        if (!invalidInput())
        {
            EditableDefaultFieldLayout editableDefaultFieldLayout = getFieldLayoutManager().getEditableDefaultFieldLayout();
            // Create a field layout with the same field properties as the default field layout
            EditableFieldLayout editableFieldLayout = new EditableFieldLayoutImpl(null, editableDefaultFieldLayout.getFieldLayoutItems());
            editableFieldLayout.setName(getFieldLayoutName());
            editableFieldLayout.setDescription(getFieldLayoutDescription());
            getFieldLayoutManager().storeEditableFieldLayout(editableFieldLayout);
            return redirectToView();
        }

        return getResult();
    }

    @RequiresXsrfCheck
    public String doDeleteFieldLayout()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                if (!Boolean.valueOf(getConfirm()).booleanValue())
                {
                    return "confirm";
                }

                getFieldLayoutManager().deleteFieldLayout(getFieldLayout());
                return redirectToView();
            }
        }

        return getResult();
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public String doEditFieldLayout()
    {
        validateId();

        if (!invalidInput())
        {
            validateFieldLayout();

            if (!invalidInput())
            {
                if (!Boolean.valueOf(getConfirm()).booleanValue())
                {
                    return "confirm";
                }

                getFieldLayoutManager().deleteFieldLayout(getFieldLayout());
                return redirectToView();
            }
        }

        return getResult();
    }

    public Collection<FieldConfigurationScheme> getFieldLayoutSchemes(EditableFieldLayout editableFieldLayout)
    {
        try
        {
            if (!fieldLayoutSchemeMap.containsKey(editableFieldLayout.getId()))
            {
                fieldLayoutSchemeMap.put(editableFieldLayout.getId(), getFieldLayoutManager().getFieldConfigurationSchemes(editableFieldLayout));
            }
        }
        catch (Exception e)
        {
            log.error(e);
        }

        return fieldLayoutSchemeMap.get(editableFieldLayout.getId());
    }
}
