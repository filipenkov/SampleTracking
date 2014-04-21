package com.atlassian.streams.api.common;

import java.util.ArrayList;
import java.util.LinkedList;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import static com.atlassian.streams.api.common.Iterables.mergeSorted;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;

public class IterablesMergeSortedTest
{
    @Test
    public void assertThatMergingEmptyIterablesGivesAnEmptyIterable()
    {
        assertThat(
                mergeSorted(ImmutableList.of(new ArrayList<String>(), new LinkedList<String>()), Ordering.<String>natural()),
                is(emptyIterable(String.class)));
    }

    @Test
    public void assertThatMergingNonEmptyAndEmptyIterablesGivesTheMergedIterable()
    {
        assertThat(
                mergeSorted(ImmutableList.of(ImmutableList.of("a"), ImmutableList.<String>of()), Ordering.<String>natural()),
                contains("a"));
    }

    @Test
    public void assertThatMergingEmptyAndNonEmptyIterablesGivesTheMergedIterable()
    {
        assertThat(
            mergeSorted(ImmutableList.of(ImmutableList.<String>of(), ImmutableList.of("a")), Ordering.<String>natural()),
            contains("a"));
    }

    @Test
    public void assertThatMergingNonEmptyIterablesInOrderGivesMergedIterable()
    {
        assertThat(
                mergeSorted(ImmutableList.of(ImmutableList.of("a"), ImmutableList.of("b")), Ordering.<String>natural()),
                contains("a", "b"));
    }

    @Test
    public void assertThatMergingNonEmptyIterablesOutOfOrderGivesMergedIterable()
    {
        assertThat(
                mergeSorted(ImmutableList.of(ImmutableList.of("b"), ImmutableList.of("a")), Ordering.<String>natural()),
                contains("a", "b"));
    }

    @Test
    public void assertThatMergingNonEmptyIterablesOutOfOrderGivesMergedIterableInOrder()
    {
        assertThat(
                mergeSorted(ImmutableList.of(ImmutableList.of("b", "d"), ImmutableList.of("a", "c", "e")), Ordering.<String>natural()),
                contains("a", "b", "c", "d", "e"));
    }

    private static <A> Matcher<java.lang.Iterable<A>> emptyIterable(Class<A> a)
    {
        return Matchers.<A>emptyIterable();
    }
}
