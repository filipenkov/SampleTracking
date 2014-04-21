package com.atlassian.labs.jira4compat.impl;

import com.atlassian.crowd.embedded.api.Group;

/**
 *
 */
public class OsGroupAdapter implements Group
{
    private final com.opensymphony.user.Group group;

    /**
     * Static constructor of an OsGroupAdapter.
     * Returns null if the group passed is null
     *
     * @param group
     * @return
     */
    public static OsGroupAdapter build(com.opensymphony.user.Group group)
    {
        if (group == null)
        {
            return null;
        }
        return new OsGroupAdapter(group);
    }

    private OsGroupAdapter(com.opensymphony.user.Group group)
    {
        this.group = group;
    }

    public String getName()
    {
        return group.getName();
    }

    public int compareTo(Group group)
    {
        return group.compareTo(group);
    }
}
