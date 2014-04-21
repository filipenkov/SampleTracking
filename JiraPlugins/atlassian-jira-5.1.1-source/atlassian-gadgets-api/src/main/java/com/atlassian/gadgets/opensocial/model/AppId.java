package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of the identifier for a particular application, e.g. gadget type such as the Chart Gadget.
 *
 *  @since 2.0
 */
@Immutable
public final class AppId
{
    private final String id;

    /**
     * Creates a new AppId that wraps the id passed in
     * @param id the {@code String} id stored internally
     */
    public AppId(String id)
    {
        if (id == null)
        {
            throw new NullPointerException("id parameter must not be null when creating a new AppId");
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
     * Convert the {@code String} value to a {@code AppId} object.
     *
     * @param id {@code String} value to use
     * @return {@code AppId} with the underlying value of {@code id}
     */
    public static AppId valueOf(String id)
    {
        return new AppId(id);
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
        return id.equals(((AppId)o).id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}