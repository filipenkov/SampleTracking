package com.atlassian.gadgets.opensocial.model;

import net.jcip.annotations.Immutable;


/**
 * Representation of the unique identifier for a person. This ID must be nonempty and may only contain alphanumeric characters, underscore, dot, or dash,
 * and must uniquely identify the person in the container (see {@link http://www.opensocial.org/Technical-Resources/opensocial-spec-v09/OpenSocial-Specification.html#rfc.section.4.1.1.1})
 *
 *  @since 2.0
 */
@Immutable
public final class PersonId
{
    private final String id;

    /**
     * Creates a new PersonId that wraps the id passed in
     * @param id the {@code String} id stored internally
     */
    public PersonId(String id)
    {
        if (id == null)
        {
            throw new NullPointerException("id parameter must not be null when creating a new PersonId");
        }
        if (!isValidPersonId(id))
        {
            throw new IllegalArgumentException("Invalid characters in person identifier: " + id + ". Identifiers may only contain alphanumeric characters, underscore, dot, or dash.");
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
     * Convert the {@code String} value to a {@code PersonId} object.
     *
     * @param id {@code String} value to use
     * @return {@code PersonId} with the underlying value of {@code id}
     */
    public static PersonId valueOf(String id)
    {
        return new PersonId(id);
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
        return id.equals(((PersonId)o).id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    private static boolean isValidPersonId(String str)
    {
        if (str.length() == 0)
        {
            return false;
        }
        for (char nextChar : str.toCharArray())
        {
            if (!Character.isLetterOrDigit(nextChar) && nextChar != '_' && nextChar != '.' && nextChar != '-')
            {
                return false;
            }
        }
        return true;
    }
}