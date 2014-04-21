/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.setup;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.bc.user.UserService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.FileFactory;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

public class Setup2 extends AbstractSetupAction
{
    String username;
    String fullname;
    String email;
    String password;
    String confirm;
    private String EXISTING_ADMINS = "existingadmins";
    private final UserService userService;

    private UserService.CreateUserValidationResult result;

    public Setup2(UserService userService, FileFactory fileFactory)
    {
        super(fileFactory);
        this.userService = userService;
    }

    public String doDefault() throws Exception
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        if (SetupOldUserHelper.getExistingAdmins() != null)
        {
            return EXISTING_ADMINS;
        }


        setupDevModeUserNames();

        return super.doDefault();
    }

    protected void doValidation()
    {
        // return with no error messages, doExecute() will return the already setup view
        if (setupAlready())
        {
            return;
        }

        result = userService.validateCreateUserForSignupOrSetup(
                getRemoteUser(), getUsername(), getPassword(), getConfirm(), getEmail(), getFullname());
        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
        }
    }

    protected String doExecute()
    {
        if (setupAlready())
        {
            return SETUP_ALREADY;
        }

        GenericValue administrator = null;
        try
        {
            if (SetupOldUserHelper.getExistingAdmins() != null)
            {
                return EXISTING_ADMINS;
            }

            administrator = SetupOldUserHelper.addUser(result);
            final GenericValue groupAdmins = getOrCreateGroup(DEFAULT_GROUP_ADMINS);
            final GenericValue groupDevelopers = getOrCreateGroup(DEFAULT_GROUP_DEVELOPERS);
            final GenericValue groupUsers = getOrCreateGroup(DEFAULT_GROUP_USERS);

            if ((administrator != null) && (groupAdmins != null) && (groupDevelopers != null) && (groupUsers != null))
            {
                SetupOldUserHelper.addToGroup(DEFAULT_GROUP_ADMINS, result.getUsername());
                SetupOldUserHelper.addToGroup(DEFAULT_GROUP_DEVELOPERS, result.getUsername());
                SetupOldUserHelper.addToGroup(DEFAULT_GROUP_USERS, result.getUsername());

                // Moved from Setup1_2 to enable admin users to change licenses during install (in case the license used is too
                // old for the JIRA version)
                ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.ADMINISTER, DEFAULT_GROUP_ADMINS);
            }

        }
        catch (CreateException e)
        {
            throw new RuntimeException(e);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }

        return getResult();
    }

    private void setupDevModeUserNames()
    {
        DevModeSecretSauce setupHelper = new DevModeSecretSauce();
        username = setupHelper.getSecretSauceProperty("username");
        fullname = setupHelper.getSecretSauceProperty("fullname");
        email = setupHelper.getSecretSauceProperty("email");
        password = setupHelper.getSecretSauceProperty("password");
        confirm = setupHelper.getSecretSauceProperty("password");
    }

    /**
     * try and get or create a group, if we get a problem let the user know
     *
     * @param groupName the name of the group to get or create
     * @return a Group if one is found or can be created, null otherwise
     */
    private GenericValue getOrCreateGroup(String groupName)
    {
        try
        {
            return SetupOldUserHelper.addGroup(groupName);
        }
        catch (GenericEntityException e)
        {
            addErrorMessage(getText("signup.error.group.database.immutable", groupName));
            return null;
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

    public String getFullname()
    {
        return fullname;
    }

    public void setFullname(String fullname)
    {
        this.fullname = fullname;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
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
