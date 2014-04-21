package com.atlassian.jira.imports.project.mapper;

import com.atlassian.jira.security.groups.GroupManager;

/**
 * Maps groups from old to new system. Always checks to see if the group exists when getMappedId is called.
 *
 * @since v3.13
 */
public class GroupMapper extends SimpleProjectImportIdMapperImpl
{
    private final GroupManager groupManager;

    public GroupMapper(final GroupManager groupManager)
    {
        this.groupManager = groupManager;
    }

    public String getMappedId(final String oldId)
    {
        if (groupManager.groupExists(oldId))
        {
            return oldId;
        }
        return null;
    }
}
