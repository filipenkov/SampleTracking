/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.util.FileFactory;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.opensymphony.util.TextUtils;

import java.io.File;
import java.util.Collection;

public abstract class AbstractSetupAction extends JiraWebActionSupport
{
    protected static final String SETUP_ALREADY = "setupalready";
    public static final String DEFAULT_GROUP_ADMINS = "jira-administrators";
    public static final String DEFAULT_GROUP_DEVELOPERS = "jira-developers";
    public static final String DEFAULT_GROUP_USERS = "jira-users";

    protected final FileFactory fileFactory;

    public AbstractSetupAction(FileFactory fileFactory)
    {
        this.fileFactory = fileFactory;
    }

    public boolean setupAlready()
    {
        return (getApplicationProperties().getString(APKeys.JIRA_SETUP) != null);
    }

    protected void validateFormPathParam(final String formElement, final String blankErrorMessage, final String nonUniqueErrorMessage, final String myPath, final Collection<String> otherPaths)
    {
        if (!TextUtils.stringSet(myPath))
        {
            addError(formElement, getText(blankErrorMessage));
        }
        else
        {
            for (String otherPath : otherPaths)
            {
                if (myPath.equals(otherPath))
                {
                    addError(formElement, getText(nonUniqueErrorMessage));
                }
            }
            validateSetupPath(myPath, formElement);
        }
    }

    protected void validateSetupPath(final String paramPath, final String formElement)
    {
        File attachmentDir = fileFactory.getFile(paramPath);

        if (!attachmentDir.isAbsolute())
        {
            addError(formElement, getText("setup.error.filepath.notabsolute"));
        }
        else if (!attachmentDir.exists())
        {
            try
            {
                if (!attachmentDir.mkdirs())
                {
                    addError(formElement, getText("setup.error.filepath.notexist"));
                }
            }
            catch (Exception e)
            {
                addError(formElement, getText("setup.error.filepath.notexist"));
            }
        }
        else if (!attachmentDir.isDirectory())
        {
            addError(formElement, getText("setup.error.filepath.notdir"));
        }
        else if (!attachmentDir.canWrite())
        {
            addError(formElement, getText("setup.error.filepath.notwriteable"));
        }
    }
}
