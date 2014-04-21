/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractEditFieldLayoutItem extends JiraWebActionSupport
{
    private Integer position;
    private String description;
    private String fieldNameKey;
    protected static final String ACCESS_EXCEPTION = "Error while accessing field layouts.";

    public String doDefault() throws Exception
    {
        // Retrieve the field's current description
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem();
        if (fieldLayoutItem != null)
        {
            setDescription(fieldLayoutItem.getFieldDescription());
            setFieldNameKey(fieldLayoutItem.getOrderableField().getNameKey());
        }

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldLayoutItem fieldLayoutItem = getFieldLayoutItem();
        if (fieldLayoutItem != null)
        {
            // Update the field layout item's description
            getFieldLayout().setDescription(fieldLayoutItem, getDescription());

            // Store the field layout item's description
            store();
        }

        return getRedirect(getRedirectURI());
    }

    protected abstract String getRedirectURI();

    private FieldLayoutItem getFieldLayoutItem()
    {
        if (getPosition() != null)
        {
            List fieldLayoutItems = new ArrayList(getFieldLayout().getFieldLayoutItems());
            // Need to sort here, as the order depends on the name of the fields which are i18n'ed.
            Collections.sort(fieldLayoutItems);
            if (getPosition().intValue() >= 0 && (getPosition().intValue() < fieldLayoutItems.size()))
            {
                return (FieldLayoutItem) fieldLayoutItems.get(getPosition().intValue());
            }
            else
            {
                log.error("The field layout item at position '" + getPosition() + "' does not exist.");
                addErrorMessage(getText("admin.errors.fieldlayout.field.does.not.exist", "'" + getPosition() + "'"));
            }
        }
        return null;
    }

    private void setFieldNameKey(String fieldNameKey)
    {
        this.fieldNameKey = fieldNameKey;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    /** Zero-relative position of the field */
    public Integer getPosition()
    {
        return position;
    }

    public void setPosition(Integer position)
    {
        this.position = position;
    }

    protected abstract EditableFieldLayout getFieldLayout();

    protected abstract void store();

    public String getFieldNameKey()
    {
        return fieldNameKey;
    }
}
