package com.atlassian.streams.spi;

import java.util.Iterator;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An assortment of methods for operating on {@link Iterable}s.
 */
public final class Iterables
{
    /**
     * Returns the elements of {@code unfiltered} that satisfy a predicate. The
     * resulting iterable's iterator does not support {@code remove()}.  All elements
     * in {@code unfiltered} that do not satisfy the predicate are evicted from the
     * current Hibernate session.
     *
     * @param <T> the type of elements in the {@code Iterable}
     * @param evictor the application's evictor
     * @param unfiltered the {@code Iterable} to be filtered
     * @param predicate the filter {@code Predicate}
     * @return a filtered unmodifiable {@code Iterable}
     */
    public static <T> Iterable<T> filterOrEvict(final Evictor<? super T> evictor,
                                                final Iterable<T> unfiltered,
                                                final Predicate<? super T> predicate)
    {
        checkNotNull(unfiltered);
        checkNotNull(predicate);
        checkNotNull(evictor);

        return new Iterables.FilteringIterable<T>(unfiltered, predicate, new Function<T, Void>()
        {
            public Void apply(T instance)
            {
                evictor.apply(instance);
                return null;
            }
        });
    }

    private static class FilteringIterable<T> implements Iterable<T>
    {
        private final Iterable<T> unfiltered;
        private final Predicate<? super T> predicate;
        private final Function<T,Void> filterFail;

        public FilteringIterable(final Iterable<T> unfiltered,
                                 final Predicate<? super T> predicate,
                                 final Function<T, Void> filterFail)
        {
            this.unfiltered = unfiltered;
            this.predicate = predicate;
            this.filterFail = filterFail;
        }

        @Override
        public String toString()
        {
            return com.google.common.collect.Iterables.toString(this);
        }

        public Iterator<T> iterator()
        {
            final Iterator<T> unfilteredIterator = unfiltered.iterator();
            return new AbstractIterator<T>()
            {
                @Override
                protected T computeNext()
                {
                    while (unfilteredIterator.hasNext())
                    {
                        T element = unfilteredIterator.next();
                        if (predicate.apply(element))
                        {
                            return element;
                        }
                        else
                        {
                            filterFail.apply(element);
                        }
                    }
                    return endOfData();
                }
            };
        }
    }

}
