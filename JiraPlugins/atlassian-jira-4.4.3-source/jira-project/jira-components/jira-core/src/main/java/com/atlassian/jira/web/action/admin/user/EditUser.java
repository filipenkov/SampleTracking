/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.user;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.web.action.user.GenericEditProfile;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.net.URLEncoder;

@WebSudoRequired
public class EditUser extends GenericEditProfile
{
    String editName;
    User user;
    private final CrowdService crowdService;
    private final UserManager userManager;

    public EditUser(CrowdService crowdService, UserManager userManager)
    {
        this.userManager = userManager;
        this.crowdService = crowdService;
    }


    public void doValidation()
    {
        super.doValidation();

        if (!isRemoteUserPermittedToEditSelectedUser())
        {
             addErrorMessage(getText("admin.errors.must.be.sysadmin.to.edit.sysadmin"));
        }
        if (!userManager.canUpdateUser(getEditedUser()))
        {
             addErrorMessage(getText("admin.errors.cannot.edit.user.directory.read.only"));
        }
    }

    public boolean isRemoteUserPermittedToEditSelectedUser()
    {
        return getEditedUser() != null && (isSystemAdministrator() || !getGlobalPermissionManager().hasPermission(Permissions.SYSTEM_ADMIN, getEditedUser()));
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (getApplicationProperties().getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT))
        {
            addErrorMessage(getText("admin.errors.cannot.edit.user", getJiraContactHelper().getAdministratorContactMessage(getI18nHelper())));
            return getResult();
        }

        String superResult = super.doExecute();

        if (SUCCESS.equals(superResult))
        {
            return getRedirect("ViewUser.jspa?name=" + URLEncoder.encode(editName,"UTF8"));
        }

        return superResult;
    }

    public String getEditName()
    {
        return editName;
    }

    public void setEditName(String editName)
    {
        this.editName = editName;
    }

    public User getEditedUser()
    {
        if (user == null)
        {
            user = crowdService.getUser(editName);
        }

        return user;
    }
}
