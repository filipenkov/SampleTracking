package com.atlassian.crowd.directory.cache.model;

import java.io.Serializable;

/**
 * Uniquely identify an entity in Crowd based on
 * the EntityType and name.
 */
public class EntityIdentifier implements Serializable
{
    private final EntityType type;
    private final String name;

    public EntityIdentifier(EntityType type, String name)
    {
        this.type = type;
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public EntityType getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EntityIdentifier that = (EntityIdentifier) o;

        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}