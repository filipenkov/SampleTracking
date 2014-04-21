package com.atlassian.crowd.manager.application;

import java.util.List;

import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ResultsAggregatorTest
{
    @Test
    public void sizeReportedCorrectly()
    {
        ResultsAggregator<String> a = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryForAll());
        assertEquals(0, a.size());
        a.addAll(ImmutableList.of("one", "two", "three"));
        assertEquals(3, a.size());
    }

    private static <T> Query<T> queryForAll()
    {
        return queryFor(0, EntityQuery.ALL_RESULTS);
    }

    private static <T> Query<T> queryFor(int start, int offset)
    {
        Query<T> q = (Query<T>) mock(Query.class);
        when(q.getStartIndex()).thenReturn(start);
        when(q.getMaxResults()).thenReturn(offset);
        return q;
    }

    @Test
    public void resultsAreConstrained()
    {
        Query<String> q = queryFor(1, 1);

        ResultsAggregator<String> a = ResultsAggregator.with(Functions.<String>identity(), q);
        a.addAll(ImmutableList.of("1", "2", "3"));

        assertEquals(ImmutableList.of("2"), a.constrainResults());
    }

    @Test
    public void aggregatorKnowsSizeAcrossSingleAdditions()
    {
        Query<String> q = queryFor(1, 2);

        ResultsAggregator<String> a = ResultsAggregator.with(Functions.<String>identity(), q);

        assertEquals(0, a.size());

        a.add("One");
        assertEquals(1, a.size());

        a.add("Two");
        assertEquals(2, a.size());

        a.add("Three");
        assertEquals(3, a.size());
    }

    @Test
    public void identicalResultsAreCollapsedToTheFirst()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryForAll());
        String a = new String("One");
        String b = new String("One");

        agg.addAll(ImmutableList.of(a));
        assertEquals(1, agg.size());

        agg.addAll(ImmutableList.of(b));
        assertEquals(1, agg.size());

        List<String> results = agg.constrainResults();
        assertEquals(ImmutableList.of("One"), results);
        assertSame(a, results.get(0));
        assertNotSame(b, results.get(0));
    }

    @Test
    public void identicalResultsAreCollapsedToTheFirstWithSingleResultAddition()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryForAll());
        String a = new String("One");
        String b = new String("One");

        agg.add(a);
        assertEquals(1, agg.size());

        agg.add(b);
        assertEquals(1, agg.size());

        List<String> results = agg.constrainResults();
        assertEquals(ImmutableList.of("One"), results);
        assertSame(a, results.get(0));
        assertNotSame(b, results.get(0));
    }

    @Test
    public void resultsAreInOrderOfKey()
    {
        Function<String, Integer> km = new Function<String, Integer>()
        {
            @Override
            public Integer apply(String t)
            {
                return Integer.valueOf(t);
            }
        };

        ResultsAggregator<String> agg = ResultsAggregator.with(km, ResultsAggregatorTest.<String>queryForAll());
        for (int i = 19; i >= 0; i--)
        {
            agg.addAll(ImmutableList.of(Integer.toString(i)));
        }

        List<String> results = agg.constrainResults();
        assertEquals(
                ImmutableList.of("0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
                        "10", "11", "12", "13", "14", "15", "16", "17", "18", "19"),
                results);
    }

    @Test
    public void aggregatorKnowsTotalNumberOfResultsRequired()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryFor(5, 10));
        assertEquals(15, agg.getRequiredResultCount());
    }

    @Test
    public void aggregatorKnowsTotalNumberOfResultsRequiredWhenEverythingRequested()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryForAll());
        assertEquals(EntityQuery.ALL_RESULTS, agg.getRequiredResultCount());
    }

    @Test
    public void canConstructWithDifferentGenericBounds()
    {
        Function<Object, String> func = new Function<Object, String>()
        {
            @Override
            public String apply(Object from)
            {
                return from.toString();
            }
        };

        ResultsAggregator.with(func, ResultsAggregatorTest.<String>queryForAll());
    }

    /**
     * We can't know if we're finished or not until we've queried across all directories,
     * as an entry in a later directory may appear before entries from an earlier one.
     */
    @Test
    public void entryFromLaterDirectoryMayAppearAheadOfEarlier()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryFor(0, 1));

        agg.addAll(ImmutableList.of("b"));
        agg.addAll(ImmutableList.of("a"));

        assertEquals(ImmutableList.of("a"), agg.constrainResults());
    }

    /**
     * Even if we're skipping the first few entries, we still need to include the entries from the first
     * directory as they may affect how we treat later ones.
     */
    @Test
    public void entryFromLaterDirectoryIsIgnoredDueToEarlierDirectory()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), ResultsAggregatorTest.<String>queryFor(1, 2));

        agg.addAll(ImmutableList.of("a", "b"));
        agg.addAll(ImmutableList.of("a", "b", "c"));

        assertEquals(ImmutableList.of("b", "c"), agg.constrainResults());
    }

    @Test
    public void aggregatorCanBeConstructedFromLimits()
    {
        ResultsAggregator<String> agg = ResultsAggregator.with(Functions.<String>identity(), 1, 2);
        assertEquals(3, agg.getRequiredResultCount());
    }
}
