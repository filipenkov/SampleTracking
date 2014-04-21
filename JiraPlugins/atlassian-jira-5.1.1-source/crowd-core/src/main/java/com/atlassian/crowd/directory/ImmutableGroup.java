package com.atlassian.crowd.directory;

import com.atlassian.crowd.model.group.*;

import java.util.Date;

/**
 * ImmutableGroup is an immutable implementation of InternalDirectoryGroup.
 */
public class ImmutableGroup implements InternalDirectoryGroup
{
    private final GroupType type;
    private final boolean active;
    private final String description;
    private final long directoryId;
    private final String name;
    private final boolean isLocal;
    private final Date createdDate;
    private final Date updateDate;

    public ImmutableGroup(InternalDirectoryGroup group)
    {
        type = group.getType();
        active = group.isActive();
        description = group.getDescription();
        directoryId = group.getDirectoryId();
        name = group.getName();
        isLocal = group.isLocal();
        createdDate = group.getCreatedDate();
        updateDate = group.getUpdatedDate();
    }

    public GroupType getType()
    {
        return type;
    }

    public boolean isActive()
    {
        return active;
    }

    public String getDescription()
    {
        return description;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public int compareTo(Group o)
    {
        return GroupComparator.compareTo(this,o);
    }

    public boolean isLocal()
    {
        return isLocal;
    }

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updateDate;
    }
}
