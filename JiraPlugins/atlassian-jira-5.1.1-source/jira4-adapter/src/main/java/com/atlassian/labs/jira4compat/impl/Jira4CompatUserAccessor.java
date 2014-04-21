package com.atlassian.labs.jira4compat.impl;

import com.atlassian.core.user.GroupUtils;
import com.atlassian.core.user.UserUtils;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.labs.jira4compat.api.CompatUserAccessor;
import com.opensymphony.user.EntityNotFoundException;

/**
 *
 */
public class Jira4CompatUserAccessor implements CompatUserAccessor
{
    public User findUser(String userName)
    {
        try
        {
            return OsUserAdapter.build(UserUtils.getUser(userName));
        }
        catch (EntityNotFoundException e)
        {
            //log.error("Unknown user", e);
            return null;
        }
    }

    public Group findGroup(String groupName)
    {
        return OsGroupAdapter.build(GroupUtils.getGroup(groupName));
    }
}
