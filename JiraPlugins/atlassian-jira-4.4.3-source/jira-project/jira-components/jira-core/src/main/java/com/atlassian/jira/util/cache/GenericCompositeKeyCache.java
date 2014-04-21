package com.atlassian.jira.util.cache;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.lang.Pair;
import net.jcip.annotations.ThreadSafe;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A simple, unbounded, pluggable cache with composite keys.
 *
 * @param <K> type of first element of the key
 * @param <L> type of second element of the key
 * @param <V> type of cached values
 * @since v4.2
 */
@ThreadSafe
public final class GenericCompositeKeyCache<K,L,V>
{

    /**
     * Static factory method for convenient instantiation.
     *
     * @param <M> type of first element of the key
     * @param <N> type of second element of the key
     * @param <W> type of cached values
     * @param valueRetriever value retriever for the cache 
     * @return new cache instance
     */
    public static <M,N,W> GenericCompositeKeyCache<M,N,W> newCache(Function<Pair<M,N>,W> valueRetriever)
    {
        return new GenericCompositeKeyCache<M,N,W>(valueRetriever);
    }

    private final ConcurrentMap<Pair<K,L>,V> cache = new ConcurrentHashMap<Pair<K,L>, V>();
    private final Function<Pair<K,L>, V> valueRetriever;

    public GenericCompositeKeyCache(final Function<Pair<K,L>, V> valueRetriever)
    {
        notNull("valueRetriever", valueRetriever);
        // this sucks but I don't feel like introducing Function2
        this.valueRetriever = valueRetriever;
    }
    

    public V get(K first, L second)
    {
        final Pair<K,L> theKey = buildKey(first, second);
        final V answer = getFromCache(theKey);
        if (answer == null)
        {
            return intoCache(theKey,getFromSource(theKey));
        }
        return answer;
    }

    private Pair<K,L> buildKey(K first, L second)
    {
        return Pair.of(first, second);
    }

    private V getFromCache(Pair theKey)
    {
        return cache.get(theKey);
    }


    private V getFromSource(Pair<K,L> key)
    {
        return valueRetriever.get(key);
    }

    private V intoCache(Pair<K,L> key, V value)
    {
        V actual = cache.putIfAbsent(key, value);
        return actual != null ? actual : value;
    }

}
