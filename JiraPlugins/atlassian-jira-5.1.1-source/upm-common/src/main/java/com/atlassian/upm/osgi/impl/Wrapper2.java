package com.atlassian.upm.osgi.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import com.atlassian.upm.Functions.Function2;

import com.google.common.base.Function;

import static com.atlassian.upm.Functions.CachedFunction.cache;
import static com.atlassian.upm.Functions.CachedFunction2.cache;
import static com.atlassian.upm.Functions.transform2;
import static com.atlassian.upm.Functions.transformValues2;
import static com.google.common.collect.ImmutableList.copyOf;
import static com.google.common.collect.ImmutableMap.copyOf;
import static java.util.Arrays.asList;

/**
 * A generic helper class for transforming maps of raw OSGi types
 * to maps of wrapped types and REST representations.  Subclass and
 * implement wrap() to use.
 *
 * @param <K> map key type
 * @param <V> map value type (to be transformed)
 * @param <Out> transformed value type
 */
public abstract class Wrapper2<K, V, Out>
{
    private final Function2<K, V, Out> wrapSingleton;
    private final Function2<K, V[], Iterable<Out>> wrapArray;
    private final Function2<K, Iterable<V>, Collection<Out>> wrapIterable;
    private final Function<Map<K, V>, Map<K, Out>> wrapSingletonValuedMap;
    private final Function<Map<K, V[]>, Map<K, Iterable<Out>>> wrapArrayValuedMap;
    private final Function<Map<K, Iterable<V>>, Map<K, Collection<Out>>> wrapIterableValuedMap;

    public Wrapper2(String name)
    {
        this.wrapSingleton = cache(name + ".wrapSingleton",
            new Function2<K, V, Out>()
            {
                public Out apply(@Nullable K key, @Nullable V value)
                {
                    return value == null ? null : wrap(key, value);
                }
            });

        this.wrapArray = cache(name + ".wrapArray",
            new Function2<K, V[], Iterable<Out>>()
            {
                public Iterable<Out> apply(@Nullable K key, @Nullable V[] values)
                {
                    return values == null ?
                        Collections.<Out> emptyList() :
                        copyOf(transform2(key, asList(values), wrapSingleton));
                }
            });

        this.wrapIterable = cache(name + ".wrapIterable",
            new Function2<K, Iterable<V>, Collection<Out>>()
            {
                public Collection<Out> apply(@Nullable K key, @Nullable Iterable<V> values)
                {
                    return values == null ?
                        Collections.<Out> emptyList() :
                        copyOf(transform2(key, values, wrapSingleton));
                }
            });

        this.wrapSingletonValuedMap = cache(name + ".wrapSingletonValuedMap",
            new Function<Map<K, V>, Map<K, Out>> ()
            {
                public Map<K, Out> apply(@Nullable Map<K, V> in)
                {
                    return in == null ?
                        Collections.<K, Out> emptyMap() :
                        copyOf(transformValues2(in, wrapSingleton));
                }
            });

        this.wrapArrayValuedMap = cache(name + ".wrapArrayValuedMap",
            new Function<Map<K, V[]>, Map<K, Iterable<Out>>> ()
            {
                public Map<K, Iterable<Out>> apply(@Nullable Map<K, V[]> in)
                {
                    return in == null ?
                        Collections.<K, Iterable<Out>> emptyMap() :
                        copyOf(transformValues2(in, wrapArray));
                }
            });

        this.wrapIterableValuedMap = cache(name + ".wrapIterableValuedMap",
            new Function<Map<K, Iterable<V>>, Map<K, Collection<Out>>> ()
            {
                public Map<K, Collection<Out>> apply(@Nullable Map<K, Iterable<V>> in)
                {
                    return in == null ?
                        Collections.<K, Collection<Out>> emptyMap() :
                        copyOf(transformValues2(in, wrapIterable));
                }
            });
    }

    protected abstract Out wrap(@Nullable K key, @Nullable V value);

    public final Out fromSingleton(@Nullable K key, @Nullable V value)
    {
        return wrapSingleton.apply(key, value);
    }

    public final Iterable<Out> fromArray(@Nullable K key, @Nullable V[] values)
    {
        return wrapArray.apply(key, values);
    }

    public final Collection<Out> fromIterable(@Nullable K key, @Nullable Iterable<V> values)
    {
        return wrapIterable.apply(key, values);
    }

    public final Map<K, Out> fromSingletonValuedMap(@Nullable Map<K, V> in)
    {
        return wrapSingletonValuedMap.apply(in);
    }

    public final Map<K, Iterable<Out>> fromArrayValuedMap(@Nullable Map<K, V[]> in)
    {
        return wrapArrayValuedMap.apply(in);
    }

    public final Map<K, Collection<Out>> fromIterableValuedMap(@Nullable Map<K, Iterable<V>> in)
    {
        return wrapIterableValuedMap.apply(in);
    }
}
