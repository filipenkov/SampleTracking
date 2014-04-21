package com.atlassian.jira.util.collect;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;

import java.util.Iterator;

class TransformingIterable<I, E> implements Iterable<E>
{
    static <I, E> Iterable<E> transformingIterable(@NotNull final Iterable<I> delegate, @NotNull final Function<I, E> transformer)
    {
        return new TransformingIterable<I, E>(delegate, transformer);
    }

    private final Iterable<I> delegate;
    private final Function<I, E> transformer;

    TransformingIterable(final Iterable<I> delegate, final Function<I, E> transformer)
    {
        this.delegate = notNull("delegate", delegate);
        this.transformer = notNull("transformer", transformer);
    }

    public Iterator<E> iterator()
    {
        return new TransformingIterator<I, E>(delegate.iterator(), transformer);
    }
}
