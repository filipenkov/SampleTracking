package com.atlassian.crowd.model.group;

import com.atlassian.crowd.model.EntityWithAttributes;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Map;
import java.util.Set;

/**
 * Encapsulates the concept of group which has attributes.
 */
public class InternalGroupWithAttributes extends EntityWithAttributes implements GroupWithAttributes
{
    private final InternalGroup group;

    public InternalGroupWithAttributes(final InternalGroup group, final Map<String, Set<String>> attributes)
    {
        super(attributes);
        this.group = group;
    }

    public long getDirectoryId()
    {
        return group.getDirectoryId();
    }

    public String getName()
    {
        return group.getName();
    }

    public GroupType getType()
    {
        return group.getType();
    }

    public boolean isActive()
    {
        return group.isActive();
    }

    public String getDescription()
    {
        return group.getDescription();
    }

    public InternalGroup getInternalGroup()
    {
        return group;
    }

    @Override
    public boolean equals(final Object o)
    {
        return GroupComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return GroupComparator.hashCode(this);
    }

    public int compareTo(Group o)
    {
        return GroupComparator.compareTo(this, o);
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("group", group).
                toString();
    }
}
