/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.user.UserEventDispatcher;
import com.atlassian.jira.event.user.UserEventType;
import com.atlassian.jira.user.UserUtils;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ForgotLoginDetails extends JiraWebActionSupport
{
    private final UserUtil userUtil;
    private final ApplicationProperties applicationProperties;
    private final UserManager userManager;

    private String username;
    private String email;
    private boolean forgotPassword = false;
    private boolean forgotUserName = false;
    
    private static final String FORGOT_PASSWORD = "forgotPassword";
    private static final String FORGOT_USER_NAME = "forgotUserName";

    public ForgotLoginDetails(final UserUtil userUtil, final ApplicationProperties applicationProperties, final UserManager userManager)
    {
        this.userUtil = userUtil;
        this.applicationProperties = applicationProperties;
        this.userManager = userManager;
    }

    @Override
    protected String doExecute() throws Exception
    {
        if (isExternalUserManagement() || !userManager.hasPasswordWritableDirectory())
        {
            throw new IllegalStateException("User login details can not be reset for this JIRA site.");
        }

        if (!forgotPassword && !forgotUserName)
        {
            forgotPassword = true;
            return INPUT;
        }
        if (forgotPassword)
        {
            return doPassword();
        }
        else
        {
            return doUserNames();
        }
    }

    /**
     * Processes the request when the user has indicated that he has forgotten his password.
     * @return The view to be rendered.
     */
    private String doPassword()
    {
        doPasswordValidation();
        if (invalidInput())
        {
            // Blank the email field in case the user has toggled between forgot password and forgot user-name
            // before submitting the form.
            email = "";
            return INPUT;
        }

        final User user = userUtil.getUser(username);
        final int userEventType;
        final Map eventParams;

        // Check if we are able to reset the password
        if (userManager.canUpdateUserPassword(user))
        {
            userEventType = UserEventType.USER_FORGOTPASSWORD;

            final UserUtil.PasswordResetToken passwordResetToken = userUtil.generatePasswordResetToken(user);
            eventParams = EasyMap.build("username", username, "password.token", passwordResetToken.getToken(),
                    "password.hours", passwordResetToken.getExpiryHours());
        }
        else
        {
            // For security reasons, send the user an email rather than giving UI feedback
            userEventType = UserEventType.USER_CANNOTCHANGEPASSWORD;
            eventParams = EasyMap.build("username", username);
        }

        UserEventDispatcher.dispatchEvent(userEventType, user, eventParams);

        return "password_success";
    }

    /**
     * Validates the user-name supplied to request a new password.
     */
    private void doPasswordValidation()
    {
        if (username == null || userUtil.getUser(username) == null)
        {
            addError("username", getText("forgotpassword.error.usernameDoesNotExist"));
        }
    }

    /**
     * Processes the request when the user has indicated that he has forgotten his user-name.
     * @return The view to be rendered.
     */
    private String doUserNames()
    {
        doUserNamesValidations();
        if (invalidInput())
        {
            // Blank the user-name field in case the user has toggled between forgot password and forgot user-name
            // before submitting the form.
            username = "";
            return INPUT;
        }

        List<User> users = UserUtils.getUsersByEmail(email);
        // Users may be internally or externally managed so we build 2 lists
        List<User> managedUsers = new ArrayList<User>();
        List<User> unManagedUsers = new ArrayList<User>();
        for (User user : users)
        {
            if (userManager.canUpdateUserPassword(user))
            {
                managedUsers.add(user);
            }
            else
            {
                unManagedUsers.add(user);
            }
        }
        UserEventDispatcher.dispatchEvent(UserEventType.USER_FORGOTUSERNAME, users.get(0), EasyMap.build("users", users, "managedUsers", managedUsers, "unmanagedUsers", unManagedUsers));

        return "username_success";
    }

    /**
     *  Validates the email address supplied to request a new password.
     */
    private void doUserNamesValidations()
    {
        if (UserUtils.getUsersByEmail(email).isEmpty())
        {
            addError("email", getText("forgotpassword.error.emailaddressDoesNotExist"));
        }
    }

    private boolean isExternalUserManagement()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_USER_EXTERNALMGT);
    }

    public boolean checked(String id)
    {
        if (FORGOT_PASSWORD.equals(id))
        {
            return forgotPassword;
        }
        else if (FORGOT_USER_NAME.equals(id))
        {
            return forgotUserName;
        }
        return false;
    }

    public String displayStyle(String id)
    {
        if (FORGOT_PASSWORD.equals(id))
        {
            return forgotPassword ? "" : "display:none";
        }
        else if (FORGOT_USER_NAME.equals(id))
        {
            return forgotUserName ? "" : "display:none";
        }
        return "";
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getUsername()
    {
        return username;
    }

    public void setUsername(String username)
    {
        this.username = username;
    }

    public void setForgotten(String forgotten)
    {
        forgotPassword = FORGOT_PASSWORD.equals(forgotten);
        forgotUserName = FORGOT_USER_NAME.equals(forgotten);
    }
}
