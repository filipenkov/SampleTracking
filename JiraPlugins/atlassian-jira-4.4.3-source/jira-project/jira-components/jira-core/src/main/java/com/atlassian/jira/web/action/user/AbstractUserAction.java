/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.user;

import com.opensymphony.user.User;

public class AbstractUserAction extends ViewProfile
{
    /**
     * Override this so that User Actions only work for the remote user
     */
    public User getUser()
    {
        return getRemoteUser();
    }
}
