package com.atlassian.streams.api.common;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import static com.atlassian.streams.api.common.Fold.foldl;
import static com.atlassian.streams.api.common.Option.some;
import static com.atlassian.streams.api.common.Pair.pair;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;

/**
 * Useful methods for working with {@code Pair}s of values.
 */
public final class Pairs
{
    /**
     * Make {@code Pair}s out of the values in {@code xs}.  The result will look like
     * {@code [(x0, x1), (x2, x3), ..., (xN-1, xN)]}
     *
     * @param <A> type of {@code xs}
     * @param xs list of values
     * @return pairs of values
     */
    public static <A> Iterable<Pair<A, A>> mkPairs(Iterable<A> xs)
    {
        return foldl(xs, Pair.<Iterable<Pair<A, A>>, Option<A>>pair(ImmutableList.<Pair<A, A>>of(), Option.<A>none()), Pairs.<A>mkPairs()).first();
    }

    private static <A> Function2<A, Pair<Iterable<Pair<A, A>>, Option<A>>, Pair<Iterable<Pair<A, A>>, Option<A>>> mkPairs()
    {
        return new MkPairs<A>();
    }

    private static final class MkPairs<A> implements Function2<A, Pair<Iterable<Pair<A, A>>, Option<A>>, Pair<Iterable<Pair<A, A>>, Option<A>>>
    {
        public Pair<Iterable<Pair<A, A>>, Option<A>> apply(A v, Pair<Iterable<Pair<A, A>>, Option<A>> intermediate)
        {
            for (A a : intermediate.second())
            {
                return pair(concat(intermediate.first(), ImmutableList.of(pair(a, v))), Option.<A>none());
            }
            return pair(intermediate.first(), some(v));
        }
    }

    public static <A, B> Function<A, Pair<A, B>> pairWith(B b)
    {
        return new PairWith<A, B>(b);
    }

    public static <A, B> Iterable<Pair<A, B>> pairWith(B b, Iterable<A> as)
    {
        return transform(as, Pairs.<A, B>pairWith(b));
    }

    private static final class PairWith<A, B> implements Function<A, Pair<A, B>>
    {
        private final B b;

        public PairWith(B b)
        {
            this.b = b;
        }

        public Pair<A, B> apply(A a)
        {
            return pair(a, b);
        }

        @Override
        public String toString()
        {
            return "pairWith(" + b + ")";
        }
    }

    /**
     * Creates a {@code Predicate} which evaluates {@code p} with the first value from a {@code Pair}.
     *
     * @param <A> type of the first value of the {@code Pair}
     * @param <B> type of the second value of the {@code Pair}
     * @param p {@code Predicate} to use to evaluate the first value of a {@code Pair}
     * @return {@code Predicate} which evaluates {@code p} with the first value from a {@code Pair}
     */
    public static <A, B> Predicate<Pair<A, B>> withFirst(final Predicate<A> p)
    {
        return new Predicate<Pair<A, B>>()
        {
            public boolean apply(Pair<A, B> pair)
            {
                return p.apply(pair.first());
            }

            @Override
            public String toString()
            {
                return String.format("withFirst(%s)", p);
            }
        };
    }

    /**
     * Creates a {@code Predicate} which evaluates {@code p} with the second value from a {@code Pair}.
     *
     * @param <A> type of the first value of the {@code Pair}
     * @param <B> type of the second value of the {@code Pair}
     * @param p {@code Predicate} to use to evaluate the second value of a {@code Pair}
     * @return {@code Predicate} which evaluates {@code p} with the second value from a {@code Pair}
     */
    public static <A, B> Predicate<Pair<A, B>> withSecond(final Predicate<B> p)
    {
        return new Predicate<Pair<A, B>>()
        {
            public boolean apply(Pair<A, B> pair)
            {
                return p.apply(pair.second());
            }

            @Override
            public String toString()
            {
                return String.format("withSecond(%s)", p);
            }
        };
    }

    /**
     * @param <A> type of the first value of the {@code Pair}
     * @param <B> type of the second value of the {@code Pair}
     * @return a {@code Function} which extracts the first value from a {@code Pair}
     */
    public static <A, B> Function<Pair<A, B>, A> first()
    {
        return new First<A, B>();
    }

    private static final class First<A, B> implements Function<Pair<A, B>, A>
    {
        public A apply(Pair<A, B> p)
        {
            return p.first();
        }
    }

    /**
     * @param <A> type of the first value of the {@code Pair}
     * @param <B> type of the second value of the {@code Pair}
     * @return a {@code Function} which extracts the second value from a {@code Pair}
     */
    public static <A, B> Function<Pair<A, B>, B> second()
    {
        return new Second<A, B>();
    }

    private static final class Second<A, B> implements Function<Pair<A, B>, B>
    {
        public B apply(Pair<A, B> p)
        {
            return p.second();
        }
    }
}
