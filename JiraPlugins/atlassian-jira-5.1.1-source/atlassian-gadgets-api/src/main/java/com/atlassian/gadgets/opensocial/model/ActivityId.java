package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of the unique identifier for a particular activity.
 *
 *  @since 2.0
 */
@Immutable
public final class ActivityId
{
    private final String id;

    /**
     * Creates a new ActivityId that wraps the id passed in
     * @param id the {@code String} id stored internally
     */
    public ActivityId(String id)
    {
        if (id == null)
        {
            throw new NullPointerException("id parameter must not be null when creating a new ActivityId");
        }
        this.id = id;
    }

    /**
     * Returns the value of the identifier as a {@code String}.
     *
     * @return the value of the identifier as a {@code String}
     */
    public String value()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return id;
    }

    /**
     * Convert the {@code String} value to a {@code ActivityId} object.
     *
     * @param id {@code String} value to use
     * @return {@code ActivityId} with the underlying value of {@code id}
     */
    public static ActivityId valueOf(String id)
    {
        return new ActivityId(id);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        return id.equals(((ActivityId)o).id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}