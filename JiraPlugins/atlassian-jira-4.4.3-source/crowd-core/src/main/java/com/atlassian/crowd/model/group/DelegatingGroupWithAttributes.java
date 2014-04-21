package com.atlassian.crowd.model.group;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.impl.AbstractDelegatingEntityWithAttributes;

/**
 * Implementation of GroupWithAttributes that simply delegates to an underlying Group and Attributes object.
 * <p>
 * Instances of this class will be effectively immutable so long as either:
 * <ul>
 * <li>It is constructed with immutable objects.</li>
 * or,
 * <li>The mutable objects it is constructed with are not "leaked".</li>
 * </ul>
 */
public class DelegatingGroupWithAttributes extends AbstractDelegatingEntityWithAttributes implements GroupWithAttributes
{
    private final Group group;

    public DelegatingGroupWithAttributes(Group group, Attributes attributes)
    {
        super(attributes);
        this.group = group;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Implementation of Group
    // -----------------------------------------------------------------------------------------------------------------

    public long getDirectoryId()
    {
        return group.getDirectoryId();
    }

    public String getName()
    {
        return group.getName();
    }

    public boolean isActive()
    {
        return group.isActive();
    }

    public String getDescription()
    {
        return group.getDescription();
    }

    public GroupType getType()
    {
        return group.getType();
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

    public int compareTo(Group other)
    {
        return GroupComparator.compareTo(this, other);
    }
}
