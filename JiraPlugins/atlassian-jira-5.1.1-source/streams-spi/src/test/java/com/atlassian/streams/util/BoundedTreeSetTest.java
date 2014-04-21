package com.atlassian.streams.util;

import java.util.Collection;

import com.atlassian.streams.spi.BoundedTreeSet;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoundedTreeSetTest
{
    @Test
    public void testAdd()
    {
        BoundedTreeSet<String> set = new BoundedTreeSet<String>(3, Ordering.<String>natural());
        assertTrue(set.add("2"));
        assertSetEquals(set, "2");
        assertTrue(set.add("1"));
        assertSetEquals(set, "1", "2");
        assertTrue(set.add("4"));
        assertSetEquals(set, "1", "2", "4");
        assertFalse(set.add("5"));
        assertSetEquals(set, "1", "2", "4");
        assertTrue(set.add("3"));
        assertSetEquals(set, "1", "2", "3");
    }
    
    @Test
    public void testAddDuplicate()
    {
        BoundedTreeSet<String> set = new BoundedTreeSet<String>(3, Ordering.<String>natural());
        set.addAll(ImmutableList.of("1", "2"));
        assertFalse(set.add("1"));
        assertSetEquals(set, "1", "2");
    }

    @Test
    public void testAddAll()
    {
        BoundedTreeSet<String> set = new BoundedTreeSet<String>(3, Ordering.<String>natural());
        assertTrue(set.addAll(ImmutableList.of("2", "1", "4")));
        assertSetEquals(set, "1", "2", "4");
        assertFalse(set.addAll(ImmutableList.of("5", "3")));
        assertSetEquals(set, "1", "2", "3");
    }

    private static <T> void assertSetEquals(Collection<T> set, T... expected)
    {
        assertEquals("Sets are not equal length", expected.length, set.size());
        int i = 0;
        for (T item : set)
        {
            assertEquals("Item " + i + " not equals", expected[i], item);
            i++;
        }
    }
}