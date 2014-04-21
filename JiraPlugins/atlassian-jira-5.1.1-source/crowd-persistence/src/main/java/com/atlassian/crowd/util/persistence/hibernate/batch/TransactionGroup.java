package com.atlassian.crowd.util.persistence.hibernate.batch;

import org.apache.commons.lang.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Collection;

/**
 * A group of objects that are to be committed in one transaction.
 */
public class TransactionGroup<T extends Serializable, E extends Serializable> implements Serializable
{
    private final T primaryObject;
    private final Collection<? extends E> dependantObjects;

    public TransactionGroup(final T primaryObject, final Collection<? extends E> dependantObjects)
    {
        this.primaryObject = primaryObject;
        this.dependantObjects = dependantObjects;
    }

    public T getPrimaryObject()
    {
        return primaryObject;
    }

    public Collection<? extends E> getDependantObjects()
    {
        return dependantObjects;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionGroup that = (TransactionGroup) o;

        if (primaryObject != null ? !primaryObject.equals(that.primaryObject) : that.primaryObject != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        return primaryObject != null ? primaryObject.hashCode() : 0;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("primaryObject", primaryObject).
                toString();
    }
}
