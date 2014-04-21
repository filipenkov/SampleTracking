package com.atlassian.gadgets.dashboard.util;

import java.util.Iterator;

import org.apache.commons.lang.ObjectUtils;

import static com.atlassian.plugin.util.Assertions.notNull;

/**
 * Convenience methods for dealing with {@code Iterable}s, since we want to avoid introducing a dependency on Google Collections
 * in our public API
 */
public class Iterables
{
    private Iterables()
    {
        throw new AssertionError("Must not be instantiated");
    }

    /**
     * Compares two {@code Iterable}s to determine if they are of equal lengths and contain equal elements in the same order.
     * @param i1 the first {@code Iterable} to compare
     * @param i2 the second {@code Iterable} to compare
     * @return {@code true} if the two {@code Iterable}s contain equal elements in the same order, {@code false} otherwise
     */
    public static boolean elementsEqual(Iterable<?> i1, Iterable<?> i2)
    {
        if (i1 == i2)
        {
            return true;
        }
        if (i1 == null || i2 == null)
        {
            return false;
        }
        Iterator<?> iter1 = i1.iterator();
        Iterator<?> iter2 = i2.iterator();
        while (iter1.hasNext() && iter2.hasNext())
        {
            if (!ObjectUtils.equals(iter1.next(), iter2.next()))
            {
                return false;
            }
        }
        return !iter1.hasNext() && !iter2.hasNext();
    }

    /**
     * Checks every element of an {@code Iterable} to make sure that none of the elements are {@code null}
     * @param iterable the {@code Iterable} to check
     * @param <T> the type contained in the {@code Iterable}
     * @return the {@code Iterable} that was passed in
     */
    public static <T> Iterable<T> checkContentsNotNull(Iterable<T> iterable)
    {
        for (T element : notNull("iterable", iterable))
        {
            notNull("element of iterable", element);
        }
        return iterable;
    }
}
