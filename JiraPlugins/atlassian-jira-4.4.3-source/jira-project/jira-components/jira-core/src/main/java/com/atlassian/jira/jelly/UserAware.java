/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.opensymphony.user.User;

public interface UserAware
{
    String[] getRequiredContextVariables();

    String getUsername();

    User getUser();
}
