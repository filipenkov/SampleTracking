package com.atlassian.applinks.spi.application;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @since 3.0
 */
public class TypeId implements Comparable<TypeId>
{
    private final String id;

    public TypeId(final String id)
    {
        this.id = checkNotNull(id);
    }

    public String get()
    {
        return id;
    }

    /**
     * Convenience method for looking up {@link TypeId}s from {@link EntityType}s. The
     * {@code applinks-entity-type} descriptor enforces that all registered {@link EntityType}s implement the
     * {@link IdentifiableType} interface, so the cast in the delegate method is safe.
     */
    public static TypeId getTypeId(final EntityType type)
    {
        return get(type);
    }

    /**
     * Convenience method for looking up {@link TypeId}s from {@link ApplicationType}s. The
     * {@code applinks-application-type} descriptor enforces that all registered {@link ApplicationType}s implement the
     * {@link IdentifiableType} interface, so the cast in the delegate method is safe.
     */
    public static TypeId getTypeId(final ApplicationType type)
    {
         return get(type);
    }

    private static TypeId get(final Object type)
    {
        try
        {
            return ((IdentifiableType) type).getId();
        }
        catch (ClassCastException e)
        {
            throw new IllegalStateException(type.getClass() + " should implement " + IdentifiableType.class);
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final TypeId typeId = (TypeId) o;

        if (id != null ? !id.equals(typeId.id) : typeId.id != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return id;
    }

    public int compareTo(final TypeId o)
    {
        return id.compareTo(o.get());
    }
}
