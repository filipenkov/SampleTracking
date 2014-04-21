/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.security.AbstractPermissionManager;
import com.atlassian.jira.security.JiraPermission;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

public class MockPermissionManager extends AbstractPermissionManager
{
    Collection permissions;

    public boolean isDefaultPermission()
    {
        return defaultPermission;
    }

    public void setDefaultPermission(boolean defaultPermission)
    {
        this.defaultPermission = defaultPermission;
    }

    private boolean defaultPermission;

    public MockPermissionManager()
    {
        permissions = new HashSet();
    }

    /**
     * Creates a PermissionManager implementation where, by default, all permissions are given or denied based on the
     * given value.
     * @param defaultPermission if true, everything is permitted, if false, everything is denied.
     */
    public MockPermissionManager(final boolean defaultPermission)
    {
        this.defaultPermission = defaultPermission;
    }

    public void removeGroupPermissions(String group) throws RemoveException
    {
        for (Iterator iterator = permissions.iterator(); iterator.hasNext();)
        {
            JiraPermission jiraPermission = (JiraPermission) iterator.next();
            if (jiraPermission.getGroup().equals(group))
            {
                iterator.remove();
            }
        }
    }

    public boolean hasPermission(int permissionsId, GenericValue project, User u, boolean issueCreation)
    {
        return defaultPermission;
    }

    public boolean hasPermission(int permissionsId, User u)
    {
        return defaultPermission;
    }

}
