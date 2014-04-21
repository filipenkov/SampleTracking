package com.atlassian.upm;

import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.atlassian.upm.Pairs.ImmutablePair;
import com.atlassian.upm.api.util.Option;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.MapMaker;

import static com.atlassian.upm.api.util.Option.option;
import static com.google.common.collect.ImmutableMap.builder;
import static com.google.common.collect.Iterables.transform;
import static java.util.concurrent.TimeUnit.SECONDS;

public final class Functions
{
    public static interface Function2<F1, F2, T>
    {
        T apply(@Nullable F1 from1, @Nullable F2 from2);
    }

    public static <F1, F2, T> Function<F1, Function<F2, T>> curry(final Function2<F1, F2, T> fn)
    {
        return new Function<F1, Function<F2, T>>()
        {
            public Function<F2, T> apply(final @Nullable F1 f1)
            {
                return new Function<F2, T>()
                {
                    public T apply(final @Nullable F2 f2)
                    {
                        return fn.apply(f1, f2);
                    }
                };
            }
        };
    }

    public static <F1, F2, T> Function<ImmutablePair<F1, F2>, T> anticurry(final Function2<F1, F2, T> fn)
    {
        return new Function<ImmutablePair<F1, F2>, T>()
        {
            public T apply(@Nullable ImmutablePair<F1, F2> from)
            {
                return fn.apply(from.getFirst(), from.getSecond());
            }
        };
    }

    public static <In1, In2, Out> Iterable<Out> transform2(In1 in1, Iterable<In2> in2, Function2<In1, In2, Out> fn2)
    {
        return transform(in2, curry(fn2).apply(in1));
    }

    public static <In1, In2, Out> Map<In1, Out> transformValues2(Map<In1, In2> in, Function2<In1, In2, Out> fn2)
    {
        Builder<In1, Out> out = builder();
        for (Entry<In1, In2> entry : in.entrySet())
        {
            out.put(entry.getKey(), fn2.apply(entry.getKey(), entry.getValue()));
        }
        return out.build();
    }

    public static <F, T> Iterable<T> applyEach(Iterable<? extends Function<F, T>> fns, F from)
    {
        ImmutableList.Builder<T> out = ImmutableList.builder();
        for (Function<F, T> fn : fns)
        {
            out.add(fn.apply(from));
        }
        return out.build();
    }

    public static final class NotNullFunction<F, T> implements Function<F, Option<T>>
    {
        private final Function<F, T> fn;

        private NotNullFunction(Function<F, T> fn)
        {
            this.fn = fn;
        }

        public static <F, T> Function<F, Option<T>> notNull(Function<F, T> fn)
        {
            return new NotNullFunction<F, T>(fn);
        }

        public Option<T> apply(@Nullable F from)
        {
            return option(fn.apply(from));
        }
    }

    public static final class NotNullFunction2<F1, F2, T> implements Function2<F1, F2, Option<T>>
    {
        private final Function2<F1, F2, T> fn;

        private NotNullFunction2(Function2<F1, F2, T> fn)
        {
            this.fn = fn;
        }

        public static <F1, F2, T> Function2<F1, F2, Option<T>> notNull(Function2<F1, F2, T> fn)
        {
            return new NotNullFunction2<F1, F2, T>(fn);
        }

        public Option<T> apply(@Nullable F1 from1, @Nullable F2 from2)
        {
            return option(fn.apply(from1, from2));
        }
    }

    private static final Map<String, Function<?, ?>> functionCache = new MapMaker().expiration(10, SECONDS).makeMap();
    private static final Map<String, Function2<?, ?, ?>> function2Cache = new MapMaker().expiration(10, SECONDS).makeMap();

    public static final class CachedFunction<F, T> implements Function<F, T>
    {
        private final Map<F, Option<T>> cache;

        private CachedFunction(Function<F, T> fn)
        {
            this.cache = new MapMaker()
                .expiration(10, SECONDS)
                .makeComputingMap(NotNullFunction.notNull(fn));
        }

        @SuppressWarnings("unchecked")
        public static <F, T> Function<F, T> cache(String name, Function<F, T> fn)
        {
            synchronized(functionCache)
            {
                Function<F, T> cached = (Function<F, T>) functionCache.get(name);
                if (cached == null)
                {
                    cached = new CachedFunction<F, T>(fn);
                    functionCache.put(name, cached);
                }
                return cached;
            }
        }

        public T apply(@Nullable F f)
        {
            return cache.get(f).get();
        }
    }

    public static final class CachedFunction2<F1, F2, T> implements Function2<F1, F2, T>
    {
        private final Map<ImmutablePair<F1, F2>, Option<T>> cache;

        private CachedFunction2(Function2<F1, F2, T> fn)
        {
            this.cache = new MapMaker()
                .expiration(10, SECONDS)
                .makeComputingMap(NotNullFunction.notNull(anticurry(fn)));
        }

        @SuppressWarnings("unchecked")
        public static <F1, F2, T> Function2<F1, F2, T> cache(String name, Function2<F1, F2, T> fn)
        {
            synchronized(function2Cache)
            {
                Function2<F1, F2, T> cached = (Function2<F1, F2, T>) function2Cache.get(name);
                if (cached == null)
                {
                    cached = new CachedFunction2<F1, F2, T>(fn);
                    function2Cache.put(name, cached);
                }
                return cached;
            }
        }

        public T apply(@Nullable F1 from1, @Nullable F2 from2)
        {
            Option<T> result = cache.get(ImmutablePair.pair(from1, from2));
            for (T t : result)
            {
                return t;
            }
            return null;
        }
    }

    public static <F, T> Function<F, T> virtual(final String fnName)
    {
        return new Function<F, T>()
        {
            @SuppressWarnings("unchecked")
            public T apply(@Nullable F f)
            {
                try
                {
                    return (T) f.getClass().getMethod(fnName).invoke(f);
                }
                catch (Exception e)
                {
                    return null;
                }
            }
        };
    }

    public static <F, T> Function<F, T> getter(final String propertyName)
    {
        StringBuilder getter = new StringBuilder();
        getter.append("get");
        boolean initial = true;
        for (char c : propertyName.toCharArray())
        {
            if (c == ' ')
            {
                initial = true;
            }
            else
            {
                getter.append(initial ? Character.toUpperCase(c) : c);
                initial = false;
            }
        }
        return virtual(getter.toString());
    }
}
