package com.atlassian.upm.license.internal.host;

import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.upm.license.internal.HostApplicationDescriptor;

import static com.google.common.base.Preconditions.checkNotNull;

public class JiraApplicationDescriptor implements HostApplicationDescriptor
{
    private final UserUtil userUtil;
    
    public JiraApplicationDescriptor(UserUtil userUtil)
    {
        this.userUtil = checkNotNull(userUtil, "userUtil");
    }
    
    @Override
    public int getActiveUserCount()
    {
        return userUtil.getActiveUserCount();
    }
}
