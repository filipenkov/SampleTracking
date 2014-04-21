package com.atlassian.jira.bc.group;

import com.atlassian.core.util.collection.EasyList;

import java.util.Collection;
import java.util.List;

/**
 * This is a subclass retained for backward compatibility of plugins.
 *
 * @deprecated since v4.3.  Use {@link GroupRemoveChildMapper}.
 */
public class GroupRemoveUserMapper extends GroupRemoveChildMapper
{
    public GroupRemoveUserMapper()
    {
        super();
    }

    public GroupRemoveUserMapper(List defaultGroupNames)
    {
        super(defaultGroupNames);
    }

    @Override
    public GroupRemoveUserMapper register(String childName, String groupName)
    {
        super.register(childName, EasyList.build(groupName));
        return this;
    }

    @Override
    public GroupRemoveUserMapper register(String childName)
    {
        super.register(childName);
        return this;
    }

    @Override
    public GroupRemoveUserMapper register(String childName, Collection groupNames)
    {
        super.register(childName, groupNames);
        return this;
    }
}
