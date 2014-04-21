package com.atlassian.jira.util;

import com.atlassian.crowd.embedded.api.CrowdService;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;

public class GroupPermissionCheckerImpl implements GroupPermissionChecker
{
    private PermissionManager permissionManager;
    private CrowdService crowdService;

    public GroupPermissionCheckerImpl(PermissionManager permissionManager, CrowdService crowdService)
    {
        this.permissionManager = permissionManager;
        this.crowdService = crowdService;
    }

    public final boolean hasViewGroupPermission(String group, com.opensymphony.user.User user)
    {
        // Admins can view all groups
        if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return true;
        }

        return user != null && user.inGroup(group);
    }

    public boolean hasViewGroupPermission(final String groupName, final User user)
    {
        // Admins can view all groups
        if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
        {
            return true;
        }

        Group group = crowdService.getGroup(groupName);
        return group != null && user != null && crowdService.isUserMemberOfGroup(user, group);
    }
}
