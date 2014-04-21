package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Attributes;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupWithAttributes;

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

    public String getName()
    {
        return group.getName();
    }

    public int compareTo(final Group group)
    {
        return this.group.compareTo(group);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // equals() and hashCode()
    // -----------------------------------------------------------------------------------------------------------------

    @SuppressWarnings ({ "EqualsWhichDoesntCheckParameterClass" })
    public boolean equals(Object o)
    {
        return group.equals(o);
    }

    public int hashCode()
    {
        return group.hashCode();
    }
}
