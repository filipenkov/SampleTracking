/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.action.admin.user.UserPasswordActionHelper;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;

public class ChangePassword extends JiraWebActionSupport
{
    private String current;
    private String password;
    private String confirm;
    private String username;

    private final UserUtil userUtil;
    private final UserManager userManager;

    public ChangePassword(final UserUtil userUtil, final UserManager userManager)
    {
        this.userUtil = userUtil;
        this.userManager = userManager;
    }

    public String doDefault() throws Exception
    {
        final User current = getRemoteUser();

        if (current == null || !current.getName().equals(username))
        {
            return ERROR;
        }
        
        return super.doDefault();
    }

    protected void doValidation()
    {
        try
        {
            final User currentUser = getRemoteUser();
            if (currentUser != null && !currentUser.authenticate(current))
            {
                addError("current", getText("changepassword.current.password.incorrect"));
            }
        }
        catch (Exception e)
        {
            addErrorMessage(getText("changepassword.could.not.find.user"));
        }

        if (!TextUtils.stringSet(password))
        {
            addError("password", getText("changepassword.new.password.required"));
        }
        else if (!password.equals(confirm))
        {
            addError("confirm", getText("changepassword.new.password.confirmation.does.not.match"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final User currentUSer = getRemoteUser();
        if (currentUSer == null || !currentUSer.getName().equals(username))
        {
            return ERROR;
        }


        new UserPasswordActionHelper(this, userUtil).setPassword(currentUSer, password);
        if (invalidInput())
        {
            return ERROR;
        }
        return returnComplete();
    }

    public boolean canUpdateUserPassword()
    {
        final User currentUser = getRemoteUser();
        return userManager.canUpdateUserPassword(currentUser);
    }

    public String doSuccess()
    {
        return SUCCESS;
    }

    public void setCurrent(String current)
    {
        this.current = current;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }
}