/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.issuefields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.fields.layout.field.EditableDefaultFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.EditableFieldLayout;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class EditDefaultFieldLayoutItem extends AbstractEditFieldLayoutItem
{
    private EditableDefaultFieldLayout editableDefaultFieldLayout;

    protected String getRedirectURI()
    {
        return "ViewIssueFields.jspa";
    }

    protected EditableFieldLayout getFieldLayout()
    {
        if (editableDefaultFieldLayout == null)
        {
            try
            {
                editableDefaultFieldLayout = ComponentAccessor.getFieldManager().getFieldLayoutManager().getEditableDefaultFieldLayout();
            }
            catch (DataAccessException e)
            {
                log.error(ACCESS_EXCEPTION, e);
                addErrorMessage(getText("admin.errors.fieldlayout.access.exception"));
            }
        }
        return editableDefaultFieldLayout;
    }

    protected void store()
    {
        try
        {
            ComponentAccessor.getFieldManager().getFieldLayoutManager().storeEditableDefaultFieldLayout((EditableDefaultFieldLayout) getFieldLayout());
        }
        catch (DataAccessException e)
        {
            log.error(ACCESS_EXCEPTION, e);
            addErrorMessage(getText("admin.errors.fieldlayout.access.exception"));
        }
    }
}
