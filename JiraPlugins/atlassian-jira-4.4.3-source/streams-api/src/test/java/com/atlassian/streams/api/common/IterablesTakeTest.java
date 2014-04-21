package com.atlassian.streams.api.common;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.streams.api.common.Iterables.take;
import static com.google.common.collect.Iterables.transform;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class IterablesTakeTest
{
    @Test
    public void takeOneFromList()
    {
        assertThat(take(3, asList(1, 2, 3, 4)), contains(1, 2, 3));
    }

    @Test
    public void takeOneFromNonList()
    {
        assertThat(take(3, asIterable(1, 2, 3, 4)), contains(1, 2, 3));
    }

    @Test
    public void takeNoneFromList()
    {
        assertThat(take(0, asList(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test
    public void takeNoneFromNonList()
    {
        assertThat(take(0, asIterable(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test
    public void takeAllFromList()
    {
        assertThat(take(4, asList(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test
    public void takeAllFromNonList()
    {
        assertThat(take(4, asIterable(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test
    public void takeMoreFromList()
    {
        assertThat(take(12, asList(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test
    public void takeMoreFromNonList()
    {
        assertThat(take(12, asIterable(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test(expected = NullPointerException.class)
    public void takeNull()
    {
        take(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void takeNegative()
    {
        take(-1, emptyList());
    }

    @Test(expected = NoSuchElementException.class)
    public void takeFromListAndIteratePastEnd()
    {
        Iterator<Integer> ints = take(1, asList(1, 2)).iterator();
        ints.next();
        ints.next();
    }

    @Test(expected = NoSuchElementException.class)
    public void takeFromNonListAndIteratePastEnd()
    {
        Iterator<Integer> ints = take(1, asIterable(1, 2)).iterator();
        ints.next();
        ints.next();
    }

    static <A> Iterable<A> asIterable(A... as)
    {
        return transform(asList(as), com.google.common.base.Functions.<A>identity());
    }
}
