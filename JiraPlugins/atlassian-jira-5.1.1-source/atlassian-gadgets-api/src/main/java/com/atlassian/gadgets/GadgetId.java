package com.atlassian.gadgets;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Representation of the identifier for a particular gadget.
 */
@Immutable
public final class GadgetId implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String id;

    private GadgetId(String id)
    {
        this.id = id;
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException
    {
        ois.defaultReadObject();
        
        if (id == null)
        {
            throw new InvalidObjectException("id cannot be null");
        }
    }

    /**
     * Returns the value of the identifier as a {@code String}
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GadgetId otherId = (GadgetId) o;

        return id.equals(otherId.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Converts the {@code String} value to a {@code GadgetId} object.
     * 
     * @param id {@code String} value to use
     * @return {@code GadgetId} with the underlying value of {@code id}
     */
    public static GadgetId valueOf(String id)
    {
        return new GadgetId(id);
    }
}