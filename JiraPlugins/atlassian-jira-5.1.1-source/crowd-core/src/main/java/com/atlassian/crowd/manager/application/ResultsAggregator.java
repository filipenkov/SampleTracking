package com.atlassian.crowd.manager.application;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.crowd.embedded.api.Query;
import com.atlassian.crowd.search.query.entity.EntityQuery;
import com.atlassian.crowd.search.util.SearchResultsUtil;

import com.google.common.base.Function;

/**
 * An aggregator across results from multiple queries that may include duplicates. A provided
 * key-making function provides a distinct sortable identifier for each result, allowing duplicates
 * to be excluded and results to be provided in the correct order.
 *
 * @param <T> the type of the individual results
 */
public abstract class ResultsAggregator<T>
{
    /**
     * Include the given result in the total, with earlier results taking precedence. This means
     * that any later result that uses the same key as an earlier one will be ignored.
     *
     * @param result a single result
     */
    abstract void add(T result);

    /**
     * Include all the given results in the total, with earlier results taking precedence. This means
     * that any later result that uses the same key as an earlier one will be ignored.
     *
     * @param results an iterable collection of results
     */
    abstract void addAll(Iterable<? extends T> results);

    /**
     * @return the current number of distinct results
     */
    abstract int size();

    /**
     * Return the subset of results required by the initial query's paging parameters.
     *
     * @return a subset of the results
     */
    abstract List<T> constrainResults();

    /**
     * Return the total number of results we need to fetch for this query. This will be <code>start + maxResults</code>
     * from the original query, or {@link EntityQuery#ALL_RESULTS} if that was the original maximum. This number
     * must be fetched from each underlying directory for correct aggregation.
     *
     * @return how many results need to be fetched overall
     */
    abstract int getRequiredResultCount();

    /**
     * Create an instance that will use the provided function to uniquely identify results and that will
     * expect as many total results as indicated by the query.
     *
     * @param maker a key-making function
     * @param query an indication of how many results are required
     */
    public static <T, K extends Comparable<? super K>> ResultsAggregator<T> with(Function<? super T, ? extends K> maker, Query<? extends T> query)
    {
        return new AggregatorImpl<T, K>(maker, query);
    }

    public static <T, K extends Comparable<? super K>> ResultsAggregator<T> with(Function<? super T, ? extends K> maker,
            int startIndex, int maxResults)
    {
        return new AggregatorImpl<T, K>(maker, startIndex, maxResults);
    }
}

/**
 * An implementation of {@link ResultsAggregator}, kept as a separate class to hide the generic
 * key parameter from users.
 */
class AggregatorImpl<T, K extends Comparable<? super K>> extends ResultsAggregator<T>
{
    private final int startIndex, maxResults;
    private final int totalResults;
    private final Function<? super T, ? extends K> keymaker;
    private final Map<K, T> contents;

    AggregatorImpl(Function<? super T, ? extends K> keymaker, Query<? extends T> query)
    {
        this(keymaker, query.getStartIndex(), query.getMaxResults());
    }

    AggregatorImpl(Function<? super T, ? extends K> keymaker, int startIndex, int maxResults)
    {
        this.startIndex = startIndex;
        this.maxResults = maxResults;
        this.totalResults = totalResults(startIndex, maxResults);
        this.keymaker = keymaker;
        this.contents = new HashMap<K, T>();
    }

    private static int totalResults(int startIndex, int maxResults)
    {
        if (maxResults == EntityQuery.ALL_RESULTS)
        {
            return EntityQuery.ALL_RESULTS;
        }
        else
        {
            return startIndex + maxResults;
        }
    }

    @Override
    void add(T t)
    {
        K k = keymaker.apply(t);

        if (!contents.containsKey(k))
        {
            contents.put(k, t);
        }
    }

    @Override
    void addAll(Iterable<? extends T> results)
    {
        for (T t : results)
        {
            add(t);
        }
    }

    @Override
    List<T> constrainResults()
    {
        List<K> keys = new ArrayList<K>(contents.keySet());
        Collections.sort(keys);

        ArrayList<T> sorted = new ArrayList<T>(contents.size());

        for (K k : keys)
        {
            sorted.add(contents.get(k));
        }

        return SearchResultsUtil.constrainResults(sorted, startIndex, maxResults);
    }

    @Override
    public int size()
    {
        return contents.size();
    }

    @Override
    int getRequiredResultCount()
    {
        return totalResults;
    }
}
