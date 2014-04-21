package com.atlassian.jira.util.collect;

import static com.atlassian.util.concurrent.Assertions.notNull;

import com.atlassian.jira.util.Consumer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.NotNull;

import java.util.Iterator;

/**
 * {@link EnclosedIterable} that takes a decorating function and applies it when returning in the {@link Iterator}.
 *
 * @since v3.13
 */
class TransformingIterable<I, O> implements EnclosedIterable<O>
{
    private final EnclosedIterable<I> delegate;
    private final Function<I, O> transformer;

    TransformingIterable(@NotNull final EnclosedIterable<I> delegate, @NotNull final Function<I, O> transformer)
    {
        this.delegate = notNull("delegate", delegate);
        this.transformer = notNull("decorator", transformer);
    }

    public void foreach(final Consumer<O> sink)
    {
        delegate.foreach(new Consumer<I>()
        {
            public void consume(final I element)
            {
                sink.consume(transformer.get(element));
            }
        });
    }

    public boolean isEmpty()
    {
        return delegate.isEmpty();
    }

    public int size()
    {
        return delegate.size();
    }
}
