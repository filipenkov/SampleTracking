/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.user.usermapper;

import com.opensymphony.user.User;

public interface UserMapper
{
    /**
     * Return a user for this particular email address.  If no user is found, return null.
     */
    public User getUserFromEmailAddress(String emailAddress);
}
