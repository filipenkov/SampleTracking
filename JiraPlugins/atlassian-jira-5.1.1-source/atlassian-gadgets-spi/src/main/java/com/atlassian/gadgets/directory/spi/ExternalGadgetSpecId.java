package com.atlassian.gadgets.directory.spi;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Represents the unique identifier of an {@link ExternalGadgetSpec}.
 * 
 * @since 2.0
 */
@Immutable
public final class ExternalGadgetSpecId implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    private final String id;

    protected ExternalGadgetSpecId(String id)
    {
        this.id = id;
    }

    /**
     * Returns the value of this {@code ExternalGadgetSpecId} as a {@code String}.
     * @return the value of this {@code ExternalGadgetSpecId} as a {@code String}
     */
    public String value()
    {
        return id;
    }

    /**
     * Converts the specified {@code String} into an {@code ExternalGadgetSpecId}.
     * @param id the value to use for conversion
     * @return an {@code ExternalGadgetSpecId} for the specified value
     */
    public static ExternalGadgetSpecId valueOf(String id)
    {
        return new ExternalGadgetSpecId(id);
    }

    /**
     * Reads an object from the specified input stream and attempts to
     * construct it as an {@code ExternalGadgetSpecId}. Ensures that
     * class invariants are respected.
     * @param ois the stream providing the object data
     * @throws java.io.IOException if an I/O error occurs in deserialization
     * @throws ClassNotFoundException if the class to deserialize as cannot be found 
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();

        if (id == null)
        {
            throw new InvalidObjectException("id cannot be null");
        }
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == null)
        {
            return false;
        }

        if (this == other)
        {
            return true;
        }

        if (other.getClass() != ExternalGadgetSpecId.class)
        {
            return false;
        }

        ExternalGadgetSpecId that = (ExternalGadgetSpecId) other;

        return this.id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    @Override public String toString()
    {
        return id;
    }
}
