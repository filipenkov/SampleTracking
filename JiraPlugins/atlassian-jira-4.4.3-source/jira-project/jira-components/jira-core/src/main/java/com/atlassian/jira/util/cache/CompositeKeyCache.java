package com.atlassian.jira.util.cache;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;
import com.atlassian.jira.util.Supplier;
import com.google.common.collect.MapMaker;
import org.apache.lucene.index.IndexReader;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Cache of (R, S) -> T. Designed to be used for general mappings of things to a field in an index.
 * <p>
 * <strong>Usage:</strong>
 * <pre>
 * CompositeKeyCache&lt;IndexReader, String, Collection&lt;String&gt;[]&gt; cache = CompositeKeyCache.createWeakFirstKeySoftValueCache();
 * cache.get(reader, fieldName, 
 *     new Supplier&lt;Collection&lt;String&gt;[]&gt;()
 *     {
 *         public Collection&lt;String&gt;[] get()
 *         {
 *             return doStuff(reader, fieldName);
 *         }
 *     }
 * );
 * </pre>
 * @param <R> the first key, usually a string. Must be a type that can be used as a map key (ie. immutable with correct equals/hashcode).
 * @param <S> the field name, usually a string. Must be a type that can be used as a map key (ie. immutable with correct equals/hashcode).
 * @param <T> the result thing.
 */
public class CompositeKeyCache<R, S, T>
{
    /**
     *  This cache caches the first (R) reference weakly, the second (S) reference strongly and the 
     * value (T) reference softly. This is specifically designed for use with Lucene where the 
     * {@link IndexReader} is being recycled regularly (the R) and the terms (T) may be softly referenced.
     * 
     * @param <R> the first key type
     * @param <S> the second key type
     * @param <T> the value type
     * @return a cache with weak references to the first key and soft references to the value.
     */
    public static <R, S, T> CompositeKeyCache<R, S, T> createWeakFirstKeySoftValueCache()
    {
        return new CompositeKeyCache<R, S, T>();
    }

    private final Cache cache = new Cache();

    CompositeKeyCache()
    {}

    /**
     * Get the thing mapped to this key for the specified reader.
     * 
     * @param one the first one
     * @param two the second one
     * @param supplier to generate the value if not already there, only called if not already cached.
     * @return the cached value
     */
    public T get(@NotNull final R one, @NotNull final S two, final Supplier<T> supplier)
    {
        return cache.get(one).get(new Key<S, T>(two, supplier));
    }

    /**
     * Weakly map the R to a map of the second reference to the value.
     */
    private class Cache implements Function<R, Function<Key<S, T>, T>>
    {
        private final Map<R, Function<Key<S, T>, T>> map = new MapMaker().weakKeys().makeComputingMap(
        // create the thing that holds the actual values
            new com.google.common.base.Function<R, Function<Key<S, T>, T>>()
            {
                public Function<Key<S, T>, T> apply(final R from)
                {
                    return new ValueMap<S, T>();
                }
            });

        public Function<Key<S, T>, T> get(final R from)
        {
            return map.get(from);
        }
    }

    /**
     * Hold the actual value, mapped from a Key.
     */
    static class ValueMap<S, T> implements Function<Key<S, T>, T>
    {
        private final Map<Key<S, T>, T> map = new MapMaker().softValues().makeComputingMap(
        // create the thing that holds the actual value, just an adapter from our function to a google function
            new com.google.common.base.Function<Key<S, T>, T>()
            {
                public T apply(final Key<S, T> from)
                {
                    return from.get();
                }
            });

        ValueMap()
        {}

        public T get(final Key<S, T> key)
        {
            try
            {
                return map.get(key);
            }
            finally
            {
                key.clearReference();
            }
        }
    }

    /**
     * {@link Key} that can supply a value as well. Needs to have its contained supplier reference cleared after use.
     */
    static final class Key<S, T> implements Supplier<T>
    {
        private final S two;
        private volatile Supplier<T> valueSupplier;

        public Key(final S two, final Supplier<T> valueSupplier)
        {
            this.two = notNull("two", two);
            this.valueSupplier = notNull("valueSupplier", valueSupplier);
        }

        public T get()
        {
            if (valueSupplier == null)
            {
                throw new IllegalStateException("reference has been cleared already");
            }
            return valueSupplier.get();
        }

        public S getTwo()
        {
            return two;
        }

        void clearReference()
        {
            this.valueSupplier = null;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            final int result = 1;
            return prime * result + ((two == null) ? 0 : two.hashCode());
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            return two.equals(((Key<?, ?>) obj).two);
        }
    }
}
