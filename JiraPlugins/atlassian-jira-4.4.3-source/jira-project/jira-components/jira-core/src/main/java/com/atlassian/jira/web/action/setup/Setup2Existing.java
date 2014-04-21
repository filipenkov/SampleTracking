/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.core.user.UserUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.FileFactory;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import com.opensymphony.util.TextUtils;

public class Setup2Existing extends AbstractSetupAction
{
    String username;
    String password;
    private final UserUtil userUtil;
    private final UserManager userManager;

    public Setup2Existing(final UserUtil userUtil, final FileFactory fileFactory, final UserManager userManager)
    {
        super(fileFactory);
        this.userUtil = userUtil;
        this.userManager = userManager;
    }

    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (SetupOldUserHelper.getExistingAdmins() == null)
        {
            return "/Setup2!default.jspa";
        }

        return super.doDefault();
    }

    public String doExecute() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        return getResult();
    }

    // @todo: This code is duplicate of what's in Signup action - can we refactor both into a common class?
    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if (setupAlready())
        {
            return;
        }

        if (!TextUtils.stringSet(username) || !TextUtils.stringSet(password))
        {
            if (!TextUtils.stringSet(username))
                addError("username", getText("setup.error.specify.admin"));

            if (!TextUtils.stringSet(password))
                addError("password", getText("setup.error.specify.admin.pass"));
        }
        else
        {
            try
            {
                User user = UserUtils.getUser(username);

                if (!user.authenticate(password))
                    addError("password", getText("setup.error.badpass"));

                else if (!ManagerFactory.getPermissionManager().hasPermission(Permissions.ADMINISTER, user))
                    addError("username", getText("setup.error.usernotadmin"));
            }
            catch (EntityNotFoundException e)
            {
                addError("username", getText("setup.error.admin.notfound"));
            }
        }
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public boolean isShowForgotLogin()
    {
        return userManager.hasPasswordWritableDirectory() && !getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }
}
