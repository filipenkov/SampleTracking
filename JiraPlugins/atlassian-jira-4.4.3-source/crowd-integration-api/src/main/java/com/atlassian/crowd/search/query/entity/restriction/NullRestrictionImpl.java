package com.atlassian.crowd.search.query.entity.restriction;

/**
 * Implements a a NullRestriction interface.
 */
public class NullRestrictionImpl implements NullRestriction
{
    public static final NullRestriction INSTANCE = new NullRestrictionImpl();

    private NullRestrictionImpl()
    {
    }

    @Override
    public boolean equals(final Object o)
    {
        return o instanceof NullRestriction;
    }

    @Override
    public int hashCode()
    {
        return 1;
    }
}
