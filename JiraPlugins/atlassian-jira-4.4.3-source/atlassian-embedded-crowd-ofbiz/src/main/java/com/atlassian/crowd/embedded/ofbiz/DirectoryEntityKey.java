package com.atlassian.crowd.embedded.ofbiz;

import com.atlassian.crowd.model.DirectoryEntity;
import org.apache.commons.lang.builder.HashCodeBuilder;

import static com.atlassian.crowd.embedded.impl.IdentifierUtils.toLowerCase;

/**
 * A composite Key of directoryId and name that is used to cache Users and Groups.
*/
final class DirectoryEntityKey
{
    private long directoryId;
    private String name;

    private DirectoryEntityKey(long directoryId, String name)
    {
        this.directoryId = directoryId;
        this.name = name;
    }

    public long getDirectoryId()
    {
        return directoryId;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public final boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof DirectoryEntityKey))
        {
            return false;
        }

        DirectoryEntityKey other = (DirectoryEntityKey) o;
        return directoryId == other.directoryId && name.equals(other.name);
    }

    @Override
    public final int hashCode()
    {
        return new HashCodeBuilder(1, 31).append(directoryId).append(name).toHashCode();
    }

    public static DirectoryEntityKey getKeyPreserveCase(long directoryId, String name)
    {
        return new DirectoryEntityKey(directoryId, name);
    }

    public static DirectoryEntityKey getKey(long directoryId, String name)
    {
        return new DirectoryEntityKey(directoryId, toLowerCase(name));
    }

    public static DirectoryEntityKey getKey(DirectoryEntity entity)
    {
        return new DirectoryEntityKey(entity.getDirectoryId(), toLowerCase(entity.getName()));
    }
}
