package com.atlassian.crowd.embedded.impl;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.GroupComparator;
import com.atlassian.util.concurrent.NotNull;

import com.google.common.base.Preconditions;

import java.io.Serializable;

/**
 * A general purpose immutable implementation of the Group interface.
 * <p/>
 * <strong>Note</strong>: This object does not allow null name.
 */
public class ImmutableGroup implements Group, Serializable
{
    private static final long serialVersionUID = -8981033575230430514L;

    private final String name;

    public ImmutableGroup(@NotNull final String name)
    {
        this.name = Preconditions.checkNotNull(name);
    }

    public String getName()
    {
        return name;
    }

    public int compareTo(final Group other)
    {
        return GroupComparator.compareTo(this, other);
    }

    @Override
    public boolean equals(final Object o)
    {
        return GroupComparator.equalsObject(this, o);
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }
}
