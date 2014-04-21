package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.model.membership.MembershipType;
import org.apache.commons.lang.builder.HashCodeBuilder;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * A composite Key of directoryId and name that is used to cache Users and Groups.
*/
final class MembershipKey
{
    private long directoryId;
    private String name;
    private MembershipType type;

    private MembershipKey(long directoryId, String name, MembershipType type)
    {
        this.directoryId = directoryId;
        this.name = name;
        this.type = type;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    public MembershipType getType() {
        return type;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof MembershipKey))
        {
            return false;
        }

        MembershipKey other = (MembershipKey) o;
        return directoryId == other.directoryId && name.equals(other.name) && type == other.type;
    }

    @Override
    public final int hashCode()
    {
        return new HashCodeBuilder(1, 31).append(directoryId).append(name).toHashCode();
    }

    public static MembershipKey getKeyPreserveCase(long directoryId, String name, MembershipType type)
    {
        return new MembershipKey(directoryId, name, type);
    }

    public static MembershipKey getKey(long directoryId, String name, MembershipType type)
    {
        return new MembershipKey(directoryId, toLowerCase(name), type);
    }

    public static MembershipKey getKey(DirectoryEntity entity, MembershipType type)
    {
        return new MembershipKey(entity.getDirectoryId(), toLowerCase(entity.getName()), type);
    }
}
