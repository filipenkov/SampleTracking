package com.atlassian.streams.api.common;

import java.util.Iterator;

import static com.atlassian.streams.api.common.Option.none;
import static com.google.common.base.Preconditions.checkArgument;

/**
 * Useful methods for working with {@link NonEmptyIterable}s
 */
public class NonEmptyIterables
{

    private NonEmptyIterables()
    {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    /**
     * Returns {@code get(as, 0}}, which is guaranteed to exist in a {@code NonEmptyIterable}
     * @param as elements to get the first value of
     * @param <A> type of elements in {@code as}
     * @return {@code get(as, 0}}
     */
    public static <A> A first(final NonEmptyIterable<A> as)
    {
        return as.iterator().next();
    }

    /**
     * Safely converts an {@code Iterable} to a {@code NonEmptyIterable}, or {@code None} if the {@code Iterable} was empty
     * @param it the iterable to convert to a {@code NonEmptyIterable}
     * @param <A> the element type
     * @return a {@code NonEmptyIterable} or {@code None} if {@code it} was empty
     */
    public static <A> Option<NonEmptyIterable<A>> from(Iterable<A> it)
    {
        if (!com.google.common.collect.Iterables.isEmpty(it))
        {
            return Option.<NonEmptyIterable<A>>some(new NonEmptyForwardingIterable<A>(it));
        }
        return none();
    }

    private static final class NonEmptyForwardingIterable<T> implements NonEmptyIterable<T>
    {
        private final Iterable<T> delegate;

        NonEmptyForwardingIterable(Iterable<T> delegate)
        {
            checkArgument(!com.google.common.collect.Iterables.isEmpty(delegate), "empty delegate");
            this.delegate = delegate;
        }
        public Iterator<T> iterator()
        {
            return delegate.iterator();
        }
    }
}
