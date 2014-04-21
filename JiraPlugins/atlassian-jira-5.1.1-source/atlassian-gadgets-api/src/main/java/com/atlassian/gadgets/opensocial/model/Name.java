package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;

/**
 * Representation of a {@link Person}'s full name
 *
 * @since 2.0
 */
@Immutable
public final class Name
{
    private final String name;

    public Name(String name)
    {
        if (name == null)
        {
            throw new NullPointerException("name parameter to Name must not be null");
        }
        this.name = name.intern();
    }

    public String value()
    {
        return name;
    }

    public String toString()
    {
        return name;
    }

    public static Name valueOf(String name)
    {
        return new Name(name);
    }

    public boolean equals(Object obj)
    {
        return obj instanceof Name && name.equals(((Name) obj).value());
    }

    public int hashCode()
    {
        return name.hashCode();
    }

}
