package com.atlassian.gadgets.dashboard;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

import net.jcip.annotations.Immutable;

/**
 * Representation of the identifier for a particular dashboard.
 */
@Immutable
public final class DashboardId implements Serializable
{
    private static final long serialVersionUID = 1L;

    private final String id;

    private DashboardId(String id)
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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DashboardId otherId = (DashboardId) o;

        return id.equals(otherId.id);
    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }

    /**
     * Convert the {@code String} value to a {@code DashboardId} object.
     * 
     * @param id {@code String} value to use
     * @return {code DashboardId} with the underlying value of {@code id}
     */
    public static DashboardId valueOf(String id)
    {
        return new DashboardId(id);
    }
}