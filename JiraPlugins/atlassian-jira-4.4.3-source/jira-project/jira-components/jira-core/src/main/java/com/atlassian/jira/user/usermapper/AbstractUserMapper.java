/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.user.usermapper;

import com.atlassian.jira.user.util.UserManager;
import com.opensymphony.user.User;

public abstract class AbstractUserMapper implements UserMapper
{
    private final UserManager userManager;

    protected AbstractUserMapper(UserManager userManager)
    {
        this.userManager = userManager;
    }

    protected User getUser(String username)
    {
        return userManager.getUser(username);
    }

    public abstract User getUserFromEmailAddress(String emailAddress);
}
