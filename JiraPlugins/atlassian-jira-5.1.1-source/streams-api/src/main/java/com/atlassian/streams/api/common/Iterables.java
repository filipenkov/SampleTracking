package com.atlassian.streams.api.common;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;

import com.atlassian.util.concurrent.LazyReference;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterators;
import com.google.common.collect.Ordering;
import com.google.common.collect.PeekingIterator;

import static com.atlassian.streams.api.common.Option.none;
import static com.atlassian.streams.api.common.Option.some;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.transform;
import static com.google.common.collect.Sets.newTreeSet;

/**
 * Useful methods for working with {@link Iterable}s.
 */
public final class Iterables
{
    private Iterables()
    {
        throw new UnsupportedOperationException("Cannot be instantiated");
    }

    /**
     * Takes the first {@code n} {@code xs} and returns them.
     *
     * @param <T> type of {@code xs}
     * @param n number of {@code xs} to take
     * @param xs list of values
     * @return first {@code n} {@code xs}
     */
    public static <T> Iterable<T> take(final int n, final Iterable<T> xs)
    {
        checkArgument(n >= 0, "Cannot take a negative number of elements");
        if (xs instanceof List)
        {
            final List<T> list = (List<T>) xs;
            return list.subList(0, n < list.size() ? n : list.size());
        }
        return new Range<T>(0, n, xs);
    }

    /**
     * Drop the first {@code n} {@code xs} and return the rest.
     *
     * @param <T> type of {@code xs}
     * @param n number of {@code xs} to drop
     * @param xs list of values
     * @return remaining {@code xs} after dropping the first {@code n}
     */
    public static <T> Iterable<T> drop(final int n, final Iterable<T> xs)
    {
        checkArgument(n >= 0, "Cannot drop a negative number of elements");
        if (xs instanceof List)
        {
            final List<T> list = (List<T>) xs;
            if (n > list.size() - 1)
            {
                return ImmutableList.of();
            }
            return ((List<T>) xs).subList(n, list.size());
        }
        return new Range<T>(n, Integer.MAX_VALUE, xs);
    }

    static final class Range<T> implements Iterable<T>
    {
        private final Iterable<T> delegate;
        private final int drop;
        private final int size;

        private Range(final int drop, final int size, final Iterable<T> delegate)
        {
            this.delegate = checkNotNull(delegate);
            this.drop = drop;
            this.size = size;
        }

        public Iterator<T> iterator()
        {
            return new Iter<T>(drop, size, delegate.iterator());
        }

        @Override
        public String toString()
        {
            return com.google.common.collect.Iterables.toString(this);
        }

        static final class Iter<T> extends AbstractIterator<T>
        {
            private final Iterator<T> it;
            private int remaining;

            Iter(final int drop, final int size, final Iterator<T> it)
            {
                this.it = it;
                this.remaining = size;

                for (int i = 0; i < drop; i++)
                {
                    if (!it.hasNext())
                    {
                        break;
                    }
                    it.next();
                }
            }

            @Override
            protected T computeNext()
            {
                if ((remaining > 0) && it.hasNext())
                {
                    remaining--;
                    return it.next();
                }
                else
                {
                    return endOfData();
                }
            }
        }
    }

    /**
     * Applies {@code f} to each element of {@code collection}, then concatenates the result.
     *
     * @param <A> type of elements in {@code collection}
     * @param <B> type elements in the new {@code Iterable} {@code f} will transform elements to
     * @param collection elements to apply {@code f} to
     * @param f {@code Function} to apply to elements of {@code collection}
     * @return concatenated result of applying {@code f} to each element of {@code collection}
     */
    public static <A, B> Iterable<B> flatMap(final Iterable<A> collection, final Function<A, Iterable<B>> f)
    {
        return concat(transform(collection, f));
    }

    /**
     * Applies each function in {@code fs} to {@code a}.
     */
    public static <A, B> Iterable<B> revMap(Iterable<? extends Function<A, B>> fs, A a)
    {
        return transform(fs, Functions.<A, B>apply(a));
    }

    /**
     * If {@code as} is empty, returns {@code none()}. Otherwise, returns {@code some(get(as, 0))}.
     * @param <A> type of elements in {@code as}
     * @param as elements to get the first value of
     * @return {@code none()} if {@code as} is empty. {@code some(get(as, 0))} otherwise
     */
    public static <A> Option<A> first(final Iterable<A> as)
    {
        final Iterator<A> i = as.iterator();
        if (!i.hasNext())
        {
            return none();
        }
        return some(i.next());
    }

    /**
     * @return {@code Predicate} which checks if an {@code Iterable} is empty
     */
    public static Predicate<Iterable<?>> isEmpty()
    {
        return new Predicate<Iterable<?>>()
        {
            public boolean apply(final Iterable<?> i)
            {
                return com.google.common.collect.Iterables.isEmpty(i);
            }
        };
    }

    /**
     * Merge a number of already sorted collections of elements into a single collection of elements, using the elements
     * natural ordering.
     *
     * @param <A> type of the elements
     * @param xss collection of already sorted collections
     * @return {@code xss} merged in a sorted order
     */
    public static <A extends Comparable<A>> Iterable<A> mergeSorted(final Iterable<? extends Iterable<A>> xss)
    {
        return mergeSorted(xss, Ordering.<A> natural());
    }

    /**
     * Merge a number of already sorted collections of elements into a single collection of elements.
     *
     * @param <A> type of the elements
     * @param xss already sorted collection of collections
     * @param ordering ordering to use when comparing elements
     * @return {@code xss} merged in a sorted order
     */
    public static <A> Iterable<A> mergeSorted(final Iterable<? extends Iterable<A>> xss, final Ordering<A> ordering)
    {
        return new MergeSortedIterable<A>(xss, ordering);
    }

    private static final class MergeSortedIterable<A> implements Iterable<A>
    {
        private final Iterable<? extends Iterable<A>> xss;
        private final Ordering<A> ordering;

        public MergeSortedIterable(final Iterable<? extends Iterable<A>> xss, final Ordering<A> ordering)
        {
            this.xss = checkNotNull(xss, "xss");
            this.ordering = checkNotNull(ordering, "ordering");
        }

        public Iterator<A> iterator()
        {
            return new Iter<A>(xss, ordering);
        }

        @Override
        public String toString()
        {
            return com.google.common.collect.Iterables.toString(this);
        }

        private static final class Iter<A> extends AbstractIterator<A>
        {
            private final TreeSet<PeekingIterator<A>> xss;

            private Iter(final Iterable<? extends Iterable<A>> xss, final Ordering<A> ordering)
            {
                this.xss = newTreeSet(peekingIteratorOrdering(ordering));
                com.google.common.collect.Iterables.addAll(this.xss, transform(filter(xss, not(isEmpty())), peekingIterator()));
            }

            @Override
            protected A computeNext()
            {
                final Option<PeekingIterator<A>> currFirstOption = first(xss);
                if (!currFirstOption.isDefined())
                {
                    return endOfData();
                }
                final PeekingIterator<A> currFirst = currFirstOption.get();

                // We remove the iterator from the set first, before we mutate it, otherwise we wouldn't be able to
                // properly find it to remove it. Mutation sucks.
                xss.remove(currFirst);

                final A next = currFirst.next();
                if (currFirst.hasNext())
                {
                    xss.add(currFirst);
                }
                return next;
            }

            private Function<? super Iterable<A>, ? extends PeekingIterator<A>> peekingIterator()
            {
                return new Function<Iterable<A>, PeekingIterator<A>>()
                {
                    public PeekingIterator<A> apply(final Iterable<A> i)
                    {
                        return Iterators.peekingIterator(i.iterator());
                    }
                };
            }

            private Ordering<? super PeekingIterator<A>> peekingIteratorOrdering(final Ordering<A> ordering)
            {
                return new Ordering<PeekingIterator<A>>()
                {
                    public int compare(final PeekingIterator<A> lhs, final PeekingIterator<A> rhs)
                    {
                        if (lhs == rhs)
                        {
                            return 0;
                        }
                        return ordering.compare(lhs.peek(), rhs.peek());
                    }
                };
            }
        }
    }

    /**
     * Makes a lazy copy of {@code xs}.
     *
     * @param <A> type of elements in {@code xs}
     * @param xs {@code Iterable} to be memoized
     * @return lazy copy of {@code as}
     */
    public static <A> Iterable<A> memoize(final Iterable<A> xs)
    {
        return new Memoizer<A>(xs);
    }

    /**
     * Memoizing iterable, maintains a lazily computed linked list of nodes.
     *
     * @param <A> the type
     */
    static final class Memoizer<A> implements Iterable<A>
    {
        private final Node<A> head;

        Memoizer(final Iterable<A> delegate)
        {
            head = nextNode(delegate.iterator());
        }

        public Iterator<A> iterator()
        {
            return new Iter<A>(head);
        }

        @Override
        public String toString()
        {
            return com.google.common.collect.Iterables.toString(this);
        }

        private static <A> Node<A> nextNode(final Iterator<A> delegate)
        {
            return delegate.hasNext() ? new Lazy<A>(delegate) : new End<A>();
        }

        /**
         * Linked list node.
         */
        interface Node<A>
        {
            boolean isEnd();

            A value();

            /**
             * Get the next Node.
             *
             * @return a new Node
             * @throws NoSuchElementException if this is terminal
             */
            Node<A> next() throws NoSuchElementException;
        }

        /**
         * Lazily computes the next node. Has a value so is not an end.
         */
        static class Lazy<A> extends LazyReference<Node<A>> implements Node<A>
        {
            private final Iterator<A> delegate;
            private final A value;

            Lazy(final Iterator<A> delegate)
            {
                this.delegate = delegate;
                this.value = delegate.next();
            }

            @Override
            protected Node<A> create() throws Exception
            {
                return nextNode(delegate);
            }

            public Node<A> next() throws NoSuchElementException
            {
                return get();
            }

            public boolean isEnd()
            {
                return false;
            }

            public A value()
            {
                return value;
            }
        }

        static class End<A> implements Node<A>
        {
            public boolean isEnd()
            {
                return true;
            }

            public Node<A> next()
            {
                throw new NoSuchElementException();
            }

            public A value()
            {
                throw new NoSuchElementException();
            }
        }

        static class Iter<A> extends AbstractIterator<A>
        {
            Node<A> node;

            Iter(final Node<A> node)
            {
                this.node = node;
            }

            @Override
            protected A computeNext()
            {
                if (node.isEnd())
                {
                    return endOfData();
                }
                try
                {
                    return node.value();
                }
                finally
                {
                    node = node.next();
                }
            }
        }
    }
}
