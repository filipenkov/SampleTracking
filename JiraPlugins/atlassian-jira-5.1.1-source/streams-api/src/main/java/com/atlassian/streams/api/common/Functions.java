package com.atlassian.streams.api.common;

import java.net.URI;
import java.util.List;
import java.util.Map;

import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.MapMaker;

import static com.atlassian.streams.api.common.Either.left;
import static com.atlassian.streams.api.common.Either.right;
import static com.atlassian.streams.api.common.Option.option;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.apache.commons.lang.StringUtils.trimToNull;

/**
 * Common function implementations.
 */
public final class Functions
{
    private Functions()
    {}

    /**
     * Get the value from a supplier.
     *
     * @param <T> the type returned, note the Supplier can be covariant.
     * @return a function that extracts the value from a supplier
     */
    public static <T> Function<Supplier<? extends T>, T> fromSupplier()
    {
        return new ValueExtractor<T>();
    }

    private static class ValueExtractor<T> implements Function<Supplier<? extends T>, T>
    {
        public T apply(final Supplier<? extends T> supplier)
        {
            return supplier.get();
        }
    }

    /**
     * Function that can be used to ignore any RuntimeExceptions that a {@link Supplier} may produce and return null instead.
     *
     * @param <T> the result type
     * @return a Function that transforms an exception into a null
     */
    public static <T> Function<Supplier<T>, Supplier<T>> ignoreExceptions()
    {
        return new ExceptionIgnorer<T>();
    }

    static class ExceptionIgnorer<T> implements Function<Supplier<T>, Supplier<T>>
    {
        public Supplier<T> apply(final Supplier<T> from)
        {
            return new IgnoreAndReturnNull<T>(from);
        }
    }

    static class IgnoreAndReturnNull<T> implements Supplier<T>
    {
        private final Supplier<T> delegate;

        IgnoreAndReturnNull(final Supplier<T> delegate)
        {
            this.delegate = checkNotNull(delegate);
        }

        public T get()
        {
            try
            {
                return delegate.get();
            }
            catch (final RuntimeException ignore)
            {
                return null;
            }
        }
    }

    public static <T> Function<T, List<T>> singletonList(Class<T> c)
    {
        return new SingletonList<T>();
    }

    private static final class SingletonList<T> implements Function<T, List<T>>
    {
        public List<T> apply(T o)
        {
            return ImmutableList.of(o);
        }
    }

    public static <F, T> Function<F, T> memoize(Function<F, T> delegate, MapMaker mapMaker)
    {
        final Map<F, T> map = mapMaker.makeComputingMap(delegate);
        return new Function<F, T>()
        {
            public T apply(F from)
            {
                return map.get(from);
            }
        };
    }

    public static Function<String, Long> parseLong()
    {
        return ParseLong.INSTANCE;
    }

    private enum ParseLong implements Function<String, Long>
    {
        INSTANCE;

        public Long apply(String s)
        {
            return Long.valueOf(s);
        }
    }

    public static Function<String, Either<NumberFormatException, Integer>> parseInt()
    {
        return ParseInt.INSTANCE;
    }

    private enum ParseInt implements Function<String, Either<NumberFormatException, Integer>>
    {
        INSTANCE;

        public Either<NumberFormatException, Integer> apply(String s)
        {
            try
            {
                return right(Integer.valueOf(s));
            }
            catch (NumberFormatException e)
            {
                return left(e);
            }
        }
    }

    public static Function<Integer, Integer> max(int i)
    {
        return new MaxInt(i);
    }

    private static final class MaxInt implements Function<Integer, Integer>
    {
        private final int i;

        public MaxInt(int i)
        {
            this.i = i;
        }

        @Override
        public Integer apply(Integer i2)
        {
            return Math.max(i, i2);
        }
    }

    public static Function<String, Option<String>> trimToNone()
    {
        return TrimToNone.INSTANCE;
    }

    private enum TrimToNone implements Function<String, Option<String>>
    {
        INSTANCE;

        public Option<String> apply(String s)
        {
            return option(trimToNull(s));
        }
    }

    public static Function<String, URI> toUri()
    {
        return ToUri.INSTANCE;
    }
    
    private enum ToUri implements Function<String, URI>
    {
        INSTANCE;
        
        public URI apply(String from)
        {
            return URI.create(from);
        }
    }

    public static Function<URI, String> uriToASCIIString()
    {
        return UriToASCIIString.INSTANCE;
    }
    
    private enum UriToASCIIString implements Function<URI, String>
    {
        INSTANCE;
        
        public String apply(URI from)
        {
            return from.toASCIIString();
        }
    }

    public static <A, B> Function<Function<A, B>, B> apply(final A a)
    {
        return new Function<Function<A,B>, B>()
        {
            public B apply(Function<A, B> f)
            {
                return f.apply(a);
            }
        };
    }
}
