package com.atlassian.jira.util.cache;

import com.atlassian.util.concurrent.LazyReference;
import com.google.common.base.Function;
import com.google.common.base.Functions;

import java.lang.ref.Reference;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A concurrent, thread-safe cache suitible for use with cached Store/Persister or Manager implementations.
 * <p />
 * The cache is filled by calls to {@link #get(Object)} by the use of a factory.
 * The cache for a given key can be invalidated with {@link #remove(Object)}, or the whole
 * invalidated with {@link #clear()}.
 * <p />
 * Important features:
 * <ul>
 * <li> Keys cannot be null.
 * <li> Nulls (that are returned by the factory) are not cached.
 * <li> Exceptions thrown by the factory are not cached.
 * <li> In some situations, the factory may be called from a thread than the one calling {@link #get(Object)}.
 * </ul>
 * 
 * Typical usage:
 *
 * <pre>
 * public class CachedFooStore implements FooStore {
 *     private final ManagedCache&lt;FooKey, Foo> cache = ManagedCache.newManagedCache(
 *         new Function&lt;FooKey, Foo>()
 *         {
 *             public Foo apply(@Nullable FooKey key)
 *             {
 *                 return delegate.get(key);
 *             }
 *         }
 *     );
 *     ...
 *     public Foo getFoo(FooKey key)
 *     {
 *         return cache.get(key);
 *     }
 *
 *     public void deleteFoo(FooKey key)
 *     {
 *         try
 *         {
 *             delegate.deleteFoo(key);
 *         }
 *         finally
 *         {
 *             cache.remove(key);
 *         }
 *      }
 *     public void updateFoo(FooKey key, Foo foo)
 *     {
 *         try
 *         {
 *             delegate.updateFoo(key, foo);
 *         }
 *         finally
 *         {
 *             cache.remove(key);
 *         }
 *      }
 *     public Foo createFoo(FooKey key, Foo foo)
 *     {
 *         try
 *         {
 *             return delegate.createFoo(key, foo);
 *         }
 *         finally
 *         {
 *             cache.remove(key);
 *         }
 *      }
 * }
 * </pre>
 *
 * @param <K> The key of the cache.
 * @param <V> The values of the cache.
 *
 * @since v4.2
 */
public final class ManagedCache<K, V> {

    /**
     * Create a new cache, where get/remove should be in terms of <code>K</code>, but the
     * internal map should be keyed in terms of a value <code>T</code> (derived from <code>K</code>
     * via <code>keyFactory</code>).
     *
     * @param factory factory for creating a new <code>V</code> when it is missing from the cache.
     * @param keyFactory function for deriving the map key <code>V</code> from <code>K</code>
     * @param <K> Keys of the cache.
     * @param <V> Values of the cache.
     * @param <T> Type of the keys in the internal map.
     */
    public static <K, V, T> ManagedCache<K, V> newManagedCache(final Function<K, V> factory, final Function<K, T> keyFactory) {
        return new ManagedCache<K,V>(factory, keyFactory);
    }

    /**
     * Create a new cache.
     * @param factory factory for creating a new <code>V</code> when it is missing from the cache.
     * @param <K> Keys of the cache.
     * @param <V> Values of the cache.
     */
    public static <K, V> ManagedCache<K, V> newManagedCache(final Function<K, V> factory) {
        return new ManagedCache<K,V>(factory, Functions.<K>identity());
    }

    private final ConcurrentMap<Object, Reference<V>> map = new ConcurrentHashMap<Object, Reference<V>>();
    private final Function<K, V> factory;
    private final Function<K, ?> keyFactory;

    private <T> ManagedCache(final Function<K, V> factory, final Function<K, T> keyFactory)
    {
        this.factory = factory;
        this.keyFactory = keyFactory;
    }

    /**
     * Retrieve an item from the cache, or create it if not already in the cache.
     */
    public V get(K arg) {
        Object key = argToKey(arg);

        Reference<V> ref = map.get(key);
        while (ref == null)
        {
            map.putIfAbsent(key, new LazyRef(arg));
            ref = map.get(key);
        }

        V result = null;
        try
        {
            result = ref.get();
        }
        finally
        {
            if (result == null) { // either was null or an exception occured
                map.remove(key, ref);
            }
        }
        return result;
    }

    private Object argToKey(K arg)
    {
        notNull("arg", arg);
        Object key = keyFactory.apply(arg);
        notNull("key", key);
        return key;
    }

    /**
     * Remove an item from the cache.
     */
    public void remove(K a) {
        map.remove(argToKey(a));
    }

    /**
     * Remove all items from the cache.
     */
    public void clear() {
        map.clear();
    }

    /**
     * Indicitive size of cache.
     * If calls to {@link #get(Object)} are in progress, and they call the factory and it returns null,
     * then those nulls may be included in this count even though they will not be
     * in the cache when the <code>get()</code> eventually returns.
     */
    public int size()
    {
        return map.size();
    }

    /**
     * A lazy reference that calls the factory with a <code>K</code> argument, but
     * once the factory is called it allows the argument to be GC-ed.
     */
    private class LazyRef extends LazyReference<V>
    {
        final AtomicReference<K> argRef;

        LazyRef(K arg)
        {
            this.argRef = new AtomicReference<K>(arg);
        }
        @Override
        protected V create() throws Exception
        {
            return factory.apply(argRef.getAndSet(null));
        }
    }
}
