/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.crowd.exception.runtime.OperationFailedException;
import com.atlassian.crowd.model.user.UserTemplate;
import com.opensymphony.util.TextUtils;

public abstract class GenericEditProfile extends ViewProfile
{
    String fullName;
    String email;

    public abstract User getEditedUser();

    public String doDefault() throws Exception
    {
        fullName = getEditedUser().getDisplayName();
        email = getEditedUser().getEmailAddress();

        return super.doDefault();
    }

    protected void doValidation()
    {
        log.debug("fullName = " + fullName);
        log.debug("email = " + email);

        //Following two addError calls replaced by Shaun during Internationalisation process
        if (!TextUtils.stringSet(TextUtils.noNull(fullName).trim()))
            addError("fullName", getText("admin.errors.invalid.full.name.specified"));

        if (!TextUtils.verifyEmail(TextUtils.noNull(email).trim()))
            addError("email", getText("admin.errors.invalid.email"));
        
        /*if (!TextUtils.stringSet(TextUtils.noNull(fullName).trim()))
            addError("fullName", "Invalid full name specified.");

        if (!TextUtils.verifyEmail(TextUtils.noNull(email).trim()))
            addError("email", "Invalid email - please check.");*/
        
    }

    protected String doExecute() throws Exception
    {
        UserTemplate user =  new UserTemplate(getEditedUser());
        user.setDisplayName(fullName);
        user.setEmailAddress(email);

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
}
