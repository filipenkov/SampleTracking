package com.atlassian.jira.web.action.admin.issuefields.screens;

import com.atlassian.jira.issue.fields.screen.FieldScreen;
import com.atlassian.jira.issue.fields.screen.FieldScreenImpl;
import com.atlassian.jira.issue.fields.screen.FieldScreenLayoutItem;
import com.atlassian.jira.issue.fields.screen.FieldScreenManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenTab;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Iterator;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@WebSudoRequired
public class EditFieldScreen extends AbstractFieldScreenAction
{
    private boolean edited;

    public EditFieldScreen(FieldScreenManager fieldScreenManager)
    {
        super(fieldScreenManager);
    }

    public String doDefault() throws Exception
    {
        validateId();

        if (invalidInput())
        {
            return ERROR;
        }
        else
        {
            setFieldScreenName(getFieldScreen().getName());
            setFieldScreenDescription(getFieldScreen().getDescription());
            return INPUT;
        }
    }

    protected void doValidation()
    {
        validateId();

        if (!invalidInput())
        {
            validateScreenName();
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        getFieldScreen().setName(getFieldScreenName());
        getFieldScreen().setDescription(getFieldScreenDescription());
        getFieldScreen().store();
        return redirectToView();
    }

    public void setFieldScreen(FieldScreen fieldScreen)
    {
        this.fieldScreen = fieldScreen;
    }

    @RequiresXsrfCheck
    public String doCopyFieldScreen()
    {
        validateId();

        if (!TextUtils.stringSet(getFieldScreenName()))
        {
            addError("fieldScreenName", getText("admin.common.errors.validname"));
        }
        else
        {
            for (Iterator iterator = getFieldScreens().iterator(); iterator.hasNext();)
            {
                FieldScreen fieldScreen = (FieldScreen) iterator.next();
                if (getFieldScreenName().equals(fieldScreen.getName()))
                {
                    addError("fieldScreenName", getText("admin.errors.screens.duplicate.screen.name"));
                }
            }
        }

        if (!invalidInput())
        {
            // Copy the screen
            FieldScreen fieldScreen = new FieldScreenImpl(getFieldScreenManager(), null);
            fieldScreen.setName(getFieldScreenName());
            fieldScreen.setDescription(getFieldScreenDescription());
            fieldScreen.store();

            // Iterate over each tab and create it
            for (Iterator iterator = getFieldScreen().getTabs().iterator(); iterator.hasNext();)
            {
                FieldScreenTab fieldScreenTab = (FieldScreenTab) iterator.next();
                FieldScreenTab copyFieldScreenTab = fieldScreen.addTab(fieldScreenTab.getName());

                for (Iterator iterator1 = fieldScreenTab.getFieldScreenLayoutItems().iterator(); iterator1.hasNext();)
                {
                    FieldScreenLayoutItem fieldScreenLayoutItem = (FieldScreenLayoutItem) iterator1.next();
                    copyFieldScreenTab.addFieldScreenLayoutItem(fieldScreenLayoutItem.getFieldId());
                }
            }

            return redirectToView();
        }

        return getResult();
    }

    public String doViewCopyFieldScreen()
    {
        validateId();

        // Get the name and description of the new screen from the user.
        setFieldScreenName(getComponentManager().getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof",getFieldScreen().getName()));
        setFieldScreenDescription(getFieldScreen().getDescription());
        return INPUT;
    }

    public boolean isEdited()
    {
        return edited;
    }

    public void setEdited(boolean edited)
    {
        this.edited = edited;
    }
}
