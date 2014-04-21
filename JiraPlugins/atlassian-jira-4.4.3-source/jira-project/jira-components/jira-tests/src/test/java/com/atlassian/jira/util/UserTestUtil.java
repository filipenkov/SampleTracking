package com.atlassian.jira.util;

import com.atlassian.jira.ManagerFactory;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;

/**
 * Handy for getting test users correctly.
 *
 * @since v3.13
 */
public class UserTestUtil
{
    public static User getUser(final String name)
    {
        return ManagerFactory.getUserManager().getUser(name);
    }

    public static Group getGroup(final String name)
    {
        return ManagerFactory.getUserManager().getGroup(name);
    }
}
