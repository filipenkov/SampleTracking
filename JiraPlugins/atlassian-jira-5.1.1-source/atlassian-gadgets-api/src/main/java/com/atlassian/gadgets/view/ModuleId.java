package com.atlassian.gadgets.view;

import net.jcip.annotations.Immutable;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Representation of the identifier for an instance of a gadget rendered on a page.
 *
 * @since 2.0
 */
@Immutable
public final class ModuleId
{
    private final long id;

    private ModuleId(long id)
    {
        this.id = id;
    }

    /**
     * Returns the value of the identifier as a {@code long}.
     *
     * @return the value of the identifier as a {@code long}
     */
    public long value()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return String.valueOf(id);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null || getClass() != obj.getClass())
        {
            return false;
        }

        ModuleId otherId = (ModuleId) obj;

        return id == otherId.id;
    }

    @Override
    public int hashCode()
    {
        // algorithm recommended by Effective Java for calculating the hashCode of long values
        // see Effective Java, Second Edition Item 9, p. 47
        return (int) (id ^ (id >>> 32));
    }

    /**
     * Converts the {@code long} value to a {@code ModuleId} object.
     *
     * @param id {@code long} value to use
     * @return {@code ModuleId} with the underlying value of {@code id}
     */
    public static ModuleId valueOf(long id)
    {
        return new ModuleId(id);
    }

    /**
     * Converts the {@code String} value to a {@code ModuleId} object.
     *
     * @param id {@code String} value to use
     * @return {@code ModuleId} with the underlying value of the {@code long} value parsed from {@code id}
     * @throws IllegalArgumentException  if {@code id} is {@code null}
     * @throws NumberFormatException if {@code id} does not contain a parsable {@code long}
     */
    public static ModuleId valueOf(String id) throws NumberFormatException
    {
        return new ModuleId(Long.parseLong(notNull("id", id)));
    }
}
