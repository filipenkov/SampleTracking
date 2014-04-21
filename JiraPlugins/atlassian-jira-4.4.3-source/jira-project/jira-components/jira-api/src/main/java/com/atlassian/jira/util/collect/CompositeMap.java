package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.collect.CollectionUtil.transformSet;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Sets.filter;
import static com.google.common.collect.Sets.union;

import com.atlassian.jira.util.Function;

import net.jcip.annotations.NotThreadSafe;

import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;

/**
 * Provides a union view of two maps.
 * <p>
 * Changes made to this map will only be made on the first map.
 * <p>
 * <strong>Note that CompositeMap is not synchronized and is not thread-safe.</strong>
 * If you wish to use this map from multiple threads concurrently, you must use
 * appropriate synchronization. The simplest approach is to wrap this map
 * using {@link java.util.Collections#synchronizedMap(Map)}. This class will behave
 * in an unspecified manner if accessed by concurrent threads without synchronization.
 */
@NotThreadSafe
public class CompositeMap<K, V> extends AbstractMap<K, V> implements Map<K, V>
{
    private final Map<K, V> one;
    private final Map<K, V> two;
    private final Set<K> removed = Sets.newHashSet();

    /**
     * Create a new CompositeMap with two composited Map instances.
     *
     * @param one  the first Map to be composited, values here override values in the second.
     * @param two  the second Map to be composited
     * @return the CompositeMap
     * @throws IllegalArgumentException if there is a key collision
     */
    public static <K, V> Map<K, V> of(final Map<K, V> one, final Map<K, V> two)
    {
        return new CompositeMap<K, V>(one, two);
    }

    /**
     * Create a new CompositeMap which composites all of the Map instances in the
     * argument. It copies the argument array, it does not use it directly.
     *
     * @param one the first Map
     * @param two the second Map
     * @throws IllegalArgumentException if there is a key collision
     */
    CompositeMap(final Map<K, V> one, final Map<K, V> two)
    {
        this.one = notNull("one", one);
        this.two = notNull("two", two);
    }

    //-----------------------------------------------------------------------

    @Override
    public Set<Entry<K, V>> entrySet()
    {
        return transformSet(keySet(), new EntryTransformer());
    }

    @Override
    public V get(final Object key)
    {
        if (removed.contains(key))
        {
            return null;
        }
        return one.containsKey(key) ? one.get(key) : two.get(key);
    }

    @Override
    public Set<K> keySet()
    {
        return filter(union(one.keySet(), two.keySet()), new Predicate<K>()
        {
            public boolean apply(final K key)
            {
                return !removed.contains(key);
            }
        });
    }

    @Override
    public boolean containsKey(final Object key)
    {
        return !removed.contains(key) && (one.containsKey(key) || two.containsKey(key));
    }

    @Override
    public boolean isEmpty()
    {
        return one.isEmpty() && two.isEmpty();
    }

    //
    // mutators
    //

    @Override
    public V put(final K key, final V value)
    {
        removed.remove(key);
        return one.put(key, value);
    }

    @Override
    public V remove(final Object key)
    {
        final V result = get(key);
        @SuppressWarnings("unchecked")
        final K k = (K) key;
        removed.add(k);
        return result;
    }

    //
    // inner classes
    //

    /**
     * Transforms a Key into a {@link Entry} that lazily evaluates the value.
     */
    class EntryTransformer implements Function<K, Entry<K, V>>
    {
        public Entry<K, V> get(final K key)
        {
            return new LazyMapEntry<K, V>(CompositeMap.this, key);
        }
    }
}
