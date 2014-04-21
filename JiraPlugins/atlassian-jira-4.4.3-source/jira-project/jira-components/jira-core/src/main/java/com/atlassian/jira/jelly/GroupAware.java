/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.jelly;

import com.opensymphony.user.Group;

public interface GroupAware
{
    public boolean hasGroup();

    public String getGroupName();

    public Group getGroup();
}
