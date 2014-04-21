package com.atlassian.streams.spi;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import com.atlassian.streams.api.common.Function2;

import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.common.Fold.foldl;

/**
 * This is a tree set that is bounded in size. When elements that are greater than the greatest are added, then the
 * greatest is discarded.
 *
 * @param <T>
 */
public class BoundedTreeSet<T> extends AbstractSet<T>
{
    private final SortedSet<T> backingSet;
    private final int maxSize;

    public BoundedTreeSet(int maxSize, Comparator<T> comparator)
    {
        this.maxSize = maxSize;
        backingSet = new TreeSet<T>(comparator);
    }

    /**
     * Returns true if the object was added. If the bound has already been reached, and this element is greater than the
     * greatest, then it doesn't get added.
     *
     * @param o The object to add.
     * @return See above
     */
    @Override
    public boolean add(T o)
    {
        boolean added = backingSet.add(o);
        if (!added)
        {
            return false;
        }
        if (backingSet.size() > maxSize)
        {
            T removed = backingSet.last();
            remove(removed);
            if (removed == o)
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true if all objects were added. If the bound has already been reached, any elements greater than
     * the greatest will not be added.
     */
    @Override
    public boolean addAll(Collection<? extends T> os)
    {
        return foldl(ImmutableList.<T>copyOf(os), true, new Function2<T, Boolean, Boolean>()
        {
            public Boolean apply(T t, Boolean allSuccess)
            {
                return add(t) && allSuccess;
            }
        });
    }

    @Override
    public Iterator<T> iterator()
    {
        return backingSet.iterator();
    }

    @Override
    public int size()
    {
        return backingSet.size();
    }
}