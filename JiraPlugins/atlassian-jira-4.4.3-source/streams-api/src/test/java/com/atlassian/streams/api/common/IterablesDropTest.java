package com.atlassian.streams.api.common;

import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.streams.api.common.Iterables.drop;
import static com.atlassian.streams.api.common.IterablesTakeTest.asIterable;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

public class IterablesDropTest
{
    @Test
    public void dropOneFromList()
    {
        assertThat(drop(1, asList(1, 2, 3, 4)), contains(2, 3, 4));
    }

    @Test
    public void dropOneFromNonList()
    {
        assertThat(drop(1, asIterable(1, 2, 3, 4)), contains(2, 3, 4));
    }

    @Test
    public void dropNoneFromList()
    {
        assertThat(drop(0, asList(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test
    public void dropNoneFromNonList()
    {
        assertThat(drop(0, asIterable(1, 2, 3, 4)), contains(1, 2, 3, 4));
    }

    @Test
    public void dropAllFromList()
    {
        assertThat(drop(4, asList(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test
    public void dropAllFromNonList()
    {
        assertThat(drop(4, asIterable(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test
    public void dropMoreFromList()
    {
        assertThat(drop(12, asList(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test
    public void dropMoreFromNonList()
    {
        assertThat(drop(12, asIterable(1, 2, 3, 4)), Matchers.<Integer> emptyIterable());
    }

    @Test(expected = NullPointerException.class)
    public void dropNull()
    {
        drop(0, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void dropNegativeFromList()
    {
        drop(-1, emptyList());
    }
}
