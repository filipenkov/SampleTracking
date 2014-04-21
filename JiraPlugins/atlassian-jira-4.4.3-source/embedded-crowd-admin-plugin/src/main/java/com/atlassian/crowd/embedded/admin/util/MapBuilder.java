package com.atlassian.crowd.embedded.admin.util;

import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSortedMap;

import com.google.common.collect.ImmutableMap;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Utility for easily creating Maps of all standard types.
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class MapBuilder<K, V>
{
    /**
     * Static factory method for creating an empty map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return an empty immutable map
     */
    public static <K, V> Map<K, V> emptyMap()
    {
        return new MapBuilder<K, V>().toMap();
    }

    /**
     * Static factory method for creating a fresh {@link MapBuilder}.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @return a new empty {@link MapBuilder}
     */
    public static <K, V> MapBuilder<K, V> newBuilder()
    {
        return new MapBuilder<K, V>();
    }

    /**
     * Static factory method for creating a fresh {@link MapBuilder} and adding the given key/value pair.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param key the key
     * @param value the value
     * @return a new empty {@link MapBuilder}
     */
    public static <K, V> MapBuilder<K, V> newBuilder(final K key, final V value)
    {
        return new MapBuilder<K, V>().add(key, value);
    }

    /**
     * Static factory method for creating a fresh {@link MapBuilder} based on the contents of a source Map.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param map the source map, may be null
     * @return a new empty {@link MapBuilder}
     */
    public static <K, V> MapBuilder<K, V> newBuilder(final Map<? extends K, ? extends V> map)
    {
        final MapBuilder<K, V> builder = newBuilder();
        builder.addAll(map);
        return builder;
    }

    /**
     * Static factory method for creating an immutable {@link Map} with a single entry.
     *
     * @param <K> the key type
     * @param <V> the value type
     * @param key the key
     * @param value the value
     * @return a new empty {@link MapBuilder}
     */
    public static <K, V> Map<K, V> singletonMap(final K key, final V value)
    {
        return Collections.singletonMap(key, value);
    }

    private final Map<K, V> map = new LinkedHashMap<K, V>();

    /**
     * prefer {@link #newBuilder()} for inferred generic arguments.
     */
    private MapBuilder()
    {}

    /**
     * Returns an immutable Map containing the given key-value pair.
     * @param key The Key
     * @param value The Value
     * @param <K> the key type
     * @param <V> the value type
     * @return an immutable Map containing the given key-value pair.
     */
    public static <K, V> Map<K, V> build(final K key, final V value)
    {
        final MapBuilder<K, V> builder = newBuilder();
        return builder.add(key, value).toMap();
    }

    /**
     * Add a key/value. Allows nulls but note that many {@link Map} implementations do not allow null
     * keys or values.
     *
     * @param key the key
     * @param value the value
     * @return this
     */
    public MapBuilder<K, V> add(final K key, final V value)
    {
        map.put(key, value);
        return this;
    }

    /**
     * Add a key/value only if the value is not null. Allows null keys but note that many {@link Map}
     * implementations do not allow null keys.
     *
     * @param key the key
     * @param value the value
     * @return this
     */
    public MapBuilder<K, V> addIfValueNotNull(final K key, final V value)
    {
        if (value != null)
        {
            add(key, value);
        }
        return this;
    }

    /**
     * Add all the entries contained in the map. Accepts a null map and does nothing.
     *
     * @param map the map to add
     * @return this
     */
    public MapBuilder<K, V> addAll(final Map<? extends K, ? extends V> map)
    {
        if (map != null)
        {
            this.map.putAll(map);
        }
        return this;
    }

    /**
     * @return an immutable {@link Map} where the entries are in no defined order.
     */
    public Map<K, V> toMap()
    {
        return unmodifiableMap(new HashMap<K, V>(map));
    }

    /**
     * @return a mutable {@link HashMap} where the entries are in no defined order.
     */
    public HashMap<K, V> toHashMap()
    {
        return new HashMap<K, V>(map);
    }

    /**
     * @return a mutable {@link LinkedHashMap} where the entries are in the same order as they were added.
     */
    public LinkedHashMap<K, V> toLinkedHashMap()
    {
        return new LinkedHashMap<K, V>(map);
    }

    /**
     * @return an immutable {@link Map} where the entries are in the same order as they were added.
     */
    public Map<K, V> toListOrderedMap()
    {
        return unmodifiableMap(toLinkedHashMap());
    }

    /**
     * @return an immutable {@link SortedMap} where the entries are in the natural order of the keys.
     *         Note that the key type <strong>must</strong> implement {@link Comparable}.
     */
    public SortedMap<K, V> toSortedMap()
    {
        return unmodifiableSortedMap(toTreeMap());
    }

    /**
     * @param comparator used to sort the keys.
     * @return an immutable {@link SortedMap} where the entries are in the order defined by the
     *         supplied {@link Comparator}.
     */
    public SortedMap<K, V> toSortedMap(final Comparator<K> comparator)
    {
        return unmodifiableSortedMap(toTreeMap(comparator));
    }

    /**
     * @return a mutable {@link TreeMap} where the entries are in the natural order of the keys.
     *         Note that the key type <strong>must</strong> implement {@link Comparable}.
     */
    public TreeMap<K, V> toTreeMap()
    {
        return new TreeMap<K, V>(map);
    }

    /**
     * @param comparator used to sort the keys.
     * @return a mutable {@link TreeMap} where the entries are in the order defined by the
     *         supplied {@link Comparator}.
     */
    public TreeMap<K, V> toTreeMap(final Comparator<K> comparator)
    {
        final TreeMap<K, V> result = new TreeMap<K, V>(comparator);
        result.putAll(map);
        return result;
    }

    /**
     * @return a mutable {@link Map} where the entries are in no defined order.
     */
    public Map<K, V> toMutableMap()
    {
        return toHashMap();
    }

    /**
     * If speed is of the essence, this will return the fastest thread-safe immutable map known.
     * Not null tolerant though.
     * @return a fast immutable sorted map
     * @throws NullPointerException if there are any nulls in this builder.
     */
    public Map<K, V> toFastMap()
    {
        return ImmutableMap.copyOf(map);
    }

    /**
     * @return an immutable map backed by a HashMap
     * @deprecated use {@link #toMap()} instead.
     */
    @Deprecated
    public Map<K, V> toImmutableMap()
    {
        return toMap();
    }
}
