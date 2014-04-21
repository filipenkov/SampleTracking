/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdDirectoryService;
import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.JiraUrlCodec;
import com.opensymphony.util.TextUtils;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class SetPassword extends ViewUser
{
    private String password;
    private String confirm;
    private final UserUtil userUtil;

    public SetPassword(CrowdService crowdService, CrowdDirectoryService crowdDirectoryService, final UserUtil userUtil, final UserPropertyManager userPropertyManager, UserManager userManager)
    {
        super(crowdService, crowdDirectoryService, userPropertyManager, userManager);
        this.userUtil = notNull("userUtil", userUtil);
    }

    protected void doValidation()
    {
        super.doValidation();
        if (!isHasPermission(Permissions.ADMINISTER))
        {
            addErrorMessage(getText("admin.errors.must.be.admin.to.set.password"));
        }
        if (!isRemoteUserPermittedToEditSelectedUser())
        {
            addErrorMessage(getText("admin.errors.must.be.sysadmin.to.set.sysadmin.password"));
        }

        if (!TextUtils.stringSet(password))
        {
            addError("password", getText("admin.errors.must.specify.a.password"));
        }
        else if (!password.equals(confirm))
        {
            addError("confirm", getText("admin.errors.two.passwords.do.not.match"));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        new UserPasswordActionHelper(this,userUtil).setPassword(getUser(), password);
        if (invalidInput())
        {
            return ERROR;
        }
        return getRedirect("/secure/admin/user/ViewUser.jspa?name=" + JiraUrlCodec.encode(getName()) + "&showPasswordUpdateMsg=true");
    }

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    public String getConfirm()
    {
        return confirm;
    }

    public void setConfirm(String confirm)
    {
        this.confirm = confirm;
    }
}
