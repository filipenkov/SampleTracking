/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.atlassian.jira.user.UserPropertyManager;
import com.opensymphony.util.TextUtils;

public abstract class GenericEditProfile extends ViewProfile
{
    String fullName;
    String email;
    boolean active;

    public GenericEditProfile(UserPropertyManager userPropertyManager)
    {
        super(userPropertyManager);
    }

    public abstract User getEditedUser();

    public String doDefault() throws Exception
    {
        fullName = getEditedUser().getDisplayName();
        email = getEditedUser().getEmailAddress();
        active = getEditedUser().isActive();

        return super.doDefault();
    }

    protected void doValidation()
    {
        log.debug("fullName = " + fullName);
        log.debug("email = " + email);

        if (!TextUtils.stringSet(TextUtils.noNull(fullName).trim()))
            addError("fullName", getText("admin.errors.invalid.full.name.specified"));

        if (!TextUtils.verifyEmail(TextUtils.noNull(email).trim()))
            addError("email", getText("admin.errors.invalid.email"));
    }

    protected String doExecute() throws Exception
    {
        UserTemplate user =  new UserTemplate(getEditedUser());
        user.setDisplayName(fullName);
        user.setEmailAddress(email);
        user.setActive(active);

        try
        {
            crowdService.updateUser(user);
        }
        catch (OperationNotPermittedException e)
        {
            addErrorMessage(getText("admin.errors.cannot.edit.user.directory.read.only"));
        }
        catch (OperationFailedException e)
        {
            addErrorMessage(getText("admin.editprofile.error.occurred", e.getMessage()));
        }

        return getResult();
    }

    public String getFullName()
    {
        return fullName;
    }

    public String getEmail()
    {
        return email;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public boolean isActive()
    {
        return active;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }
}
