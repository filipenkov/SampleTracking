package com.atlassian.crowd.directory.cache.model;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PartialSet<E> implements Serializable
{
    private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final Set<E> set = new LinkedHashSet<E>();
    private boolean complete;

    /**
     * @param singleEntity the single entity to start an incomplete set.
     */
    public PartialSet(E singleEntity)
    {
        set.add(singleEntity);
        this.complete = false;
    }


    public PartialSet(Collection<E> entities, boolean complete)
    {
        set.addAll(entities);
        this.complete = complete;
    }

    /**
     * @return Unmodifiable copy of the Set as a List.
     */
    public List<E> asList()
    {
        try
        {
            readWriteLock.readLock().lock();
            return Collections.unmodifiableList(new ArrayList(set));
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public Set<E> getUnderlyingSet()
    {
        return Collections.unmodifiableSet(set);
    }

    /**
     * @return <code>true</code> iff the collection
     * represents the complete set of results.
     */
    public boolean isComplete()
    {
        return complete;
    }

    public boolean contains(E entity)
    {
        try
        {
            readWriteLock.readLock().lock();
            return set.contains(entity);
        }
        finally
        {
            readWriteLock.readLock().unlock();
        }
    }

    public void add(E entity)
    {
        try
        {
            readWriteLock.writeLock().lock();
            set.add(entity);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public boolean remove(E entity)
    {
        if (!contains(entity))
        {
            return false;
        }

        try
        {
            readWriteLock.writeLock().lock();
            return set.remove(entity);
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

    public void clear()
    {
        try
        {
            readWriteLock.writeLock().lock();
            set.clear();
        }
        finally
        {
            readWriteLock.writeLock().unlock();
        }
    }

}