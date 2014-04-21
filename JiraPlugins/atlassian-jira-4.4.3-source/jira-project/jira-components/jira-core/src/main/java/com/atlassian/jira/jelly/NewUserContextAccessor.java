/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.opensymphony.user.User;
import org.apache.commons.jelly.JellyContext;

public interface NewUserContextAccessor
{
    public JellyContext getContext();

    public void setNewUser(String username);

    public void setNewUser(User user);

    public void loadPreviousNewUser();
}
