package com.atlassian.streams.api.common;

import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableList;

import org.junit.Test;

import static com.atlassian.streams.api.common.Iterables.memoize;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.get;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

@SuppressWarnings("unused")
public class IterablesMemoizeTest
{
    @Test
    public void assertThatFunctionTransformingSingletonIterableIsOnlyCalledOnce()
    {
        CountingFunction<Integer, String> toString = counting(toStringFunction(Integer.class));
        Iterable<String> memoized = memoize(transform(ImmutableList.of(1), toString));

        // iterate over it a few times
        for (String _ : memoized) {}
        for (String _ : memoized) {}

        assertThat(toString.count.get(), is(equalTo(1)));
    }

    @Test
    public void assertThatFunctionTransformingMultiElementIterableIsOnlyCalledOncePerElement()
    {
        CountingFunction<Integer, String> toString = counting(toStringFunction(Integer.class));
        Iterable<String> memoized = memoize(transform(ImmutableList.of(1, 2, 3, 4), toString));

        // iterate over it a few times
        for (String _ : memoized) {}
        for (String _ : memoized) {}

        assertThat(toString.count.get(), is(equalTo(4)));
    }

    @Test
    public void assertThatMemoizedTransformedIterableHasSameElementsAsOriginalIterable()
    {
        assertThat(
                memoize(transform(ImmutableList.of(1, 2, 3, 4), counting(toStringFunction(Integer.class)))),
                contains("1", "2", "3", "4"));
    }

    @Test
    public void assertThatMemoizedTransformedIterableHasSameElementsAsOriginalIterableOnSecondIteration()
    {
        Iterable<String> memoized = memoize(transform(ImmutableList.of(1, 2, 3, 4), counting(toStringFunction(Integer.class))));
        for (String _ : memoized) {}
        assertThat(
                memoized,
                contains("1", "2", "3", "4"));
    }

    @Test
    public void assertThatPredicateUsedWhenFilteringIterableIsOnlyCalledOncePerElement()
    {
        CountingPredicate<Integer> even = counting(even());
        Iterable<Integer> memoized = memoize(filter(ImmutableList.of(1, 2, 3, 4), even));

        // iterate over it a few times
        for (Integer _ : memoized) {}
        for (Integer _ : memoized) {}

        assertThat(even.count.get(), is(equalTo(4)));
    }

    @Test
    public void assertThatMemoizedFilteredIterableHasSameElementsAsOriginalIterableMinusFilteredElements()
    {
        CountingPredicate<Integer> even = counting(even());
        assertThat(memoize(filter(ImmutableList.of(1, 2, 3, 4), even)), contains(2, 4));
    }

    @Test
    public void assertThatMemoizedFilteredIterableHasSameElementsAsOriginalIterableMinusFitleredElementsOnSecondIteration()
    {
        CountingPredicate<Integer> even = counting(even());
        Iterable<Integer> memoized = memoize(filter(ImmutableList.of(1, 2, 3, 4), even));

        for (Integer _ : memoized) {}

        assertThat(memoized, contains(2, 4));
    }

    @Test
    public void assertThatIteratingHalfWayThroughMemoizedIterableAndThenIteratingCompletelyHasSameElementsOriginalIterable()
    {
        Iterable<String> memoized = memoize(transform(ImmutableList.of(1, 2, 3, 4), toStringFunction(Integer.class)));
        get(memoized, 1);
        assertThat(memoized, contains("1", "2", "3", "4"));
    }

    private <A, B> CountingFunction<A, B> counting(Function<A, B> f)
    {
        return new CountingFunction<A, B>(f);
    }

    static final class CountingFunction<A, B> implements Function<A, B>
    {
        private final Function<A, B> f;
        private final AtomicInteger count = new AtomicInteger();

        public CountingFunction(Function<A, B> f)
        {
            this.f = f;
        }

        public B apply(A a)
        {
            count.incrementAndGet();
            return f.apply(a);
        }
    }

    private <A> CountingPredicate<A> counting(Predicate<A> p)
    {
        return new CountingPredicate<A>(p);
    }

    static final class CountingPredicate<A> implements Predicate<A>
    {
        private final Predicate<A> p;
        private final AtomicInteger count = new AtomicInteger();

        public CountingPredicate(Predicate<A> p)
        {
            this.p = p;
        }

        public boolean apply(A a)
        {
            count.incrementAndGet();
            return p.apply(a);
        }
    }

    private <A> Function<A, String> toStringFunction(Class<A> aType)
    {
        return new Function<A, String>()
        {
            public String apply(A a)
            {
                return a.toString();
            }
        };
    }

    private Predicate<Integer> even()
    {
        return EvenPredicate.INSTANCE;
    }

    private enum EvenPredicate implements Predicate<Integer>
    {
        INSTANCE;

        public boolean apply(Integer i)
        {
            return i % 2 == 0;
        }
    }

}
