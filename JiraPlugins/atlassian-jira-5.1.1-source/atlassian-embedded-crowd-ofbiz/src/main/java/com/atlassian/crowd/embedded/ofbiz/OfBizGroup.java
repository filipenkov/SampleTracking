package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.group.Group;
import com.atlassian.crowd.model.group.GroupComparator;
import com.atlassian.crowd.model.group.GroupType;
import com.atlassian.crowd.model.group.InternalDirectoryGroup;
import org.apache.commons.lang.BooleanUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.Date;

import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.ACTIVE;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.LOCAL;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.CREATED_DATE;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.DESCRIPTION;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.DIRECTORY_ID;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.ID;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.NAME;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.TYPE;
import static com.atlassian.crowd.embedded.ofbiz.GroupEntity.UPDATED_DATE;
import static com.google.common.base.Preconditions.checkNotNull;

class OfBizGroup implements InternalDirectoryGroup, IdName
{
    private final long id;
    private final long directoryId;
    private final String name;
    private final boolean active;
    private final boolean local;
    private final Date createdDate;
    private final Date updatedDate;
    private final GroupType groupType;
    private final String description;

    private OfBizGroup(final GenericValue groupGenericValue)
    {
        checkNotNull(groupGenericValue);
        id = groupGenericValue.getLong(ID);
        directoryId = groupGenericValue.getLong(DIRECTORY_ID);
        name = groupGenericValue.getString(NAME);
        active = BooleanUtils.toBoolean(groupGenericValue.getInteger(ACTIVE));
        createdDate = groupGenericValue.getTimestamp(CREATED_DATE);
        updatedDate = groupGenericValue.getTimestamp(UPDATED_DATE);
        groupType = GroupType.valueOf(groupGenericValue.getString(TYPE));
        description = groupGenericValue.getString(DESCRIPTION);
        local = BooleanUtils.toBoolean(groupGenericValue.getInteger(LOCAL));
    }

    static OfBizGroup from(final GenericValue groupGenericValue)
    {
        return new OfBizGroup(checkNotNull(groupGenericValue));
    }

    public long getId()
    {
        return id;
    }

    public GroupType getType()
    {
        return groupType;
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

    public Date getCreatedDate()
    {
        return createdDate;
    }

    public Date getUpdatedDate()
    {
        return updatedDate;
    }

    public boolean isLocal()
    {
        return local;
    }

    @Override
    public final boolean equals(Object o)
    {
        return (o instanceof Group) && GroupComparator.equal(this, (Group) o);
    }

    @Override
    public final int hashCode()
    {
        return GroupComparator.hashCode(this);
    }

    public int compareTo(Group other)
    {
        return GroupComparator.compareTo(this, other);
    }
}
