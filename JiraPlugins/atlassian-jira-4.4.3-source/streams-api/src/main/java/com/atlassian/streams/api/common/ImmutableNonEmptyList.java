package com.atlassian.streams.api.common;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * An immutable non-empty list implementation. Implements {@code NonEmptyIterable}, and is backed by an
 * {@link com.google.common.collect.ImmutableList}.
 * @param <T> the type of elements stored in the list
 */
public class ImmutableNonEmptyList<T> extends ForwardingList<T> implements NonEmptyIterable<T>
{
    private final ImmutableList<T> delegate;

    public ImmutableNonEmptyList(T head)
    {
        checkNotNull(head, "head");
        this.delegate = ImmutableList.of(head);
    }

    public ImmutableNonEmptyList(T head, Iterable<T> tail)
    {
        checkNotNull(head, "head");
        this.delegate = ImmutableList.<T>builder().add(head).addAll(tail).build();
    }

    public ImmutableNonEmptyList(T head, T... others)
    {
        checkNotNull(head, "head");
        this.delegate = ImmutableList.<T>builder().add(head).addAll(Arrays.<T>asList(others)).build();
    }

    public static <E> ImmutableNonEmptyList<E> of(E e)
    {
        return new ImmutableNonEmptyList<E>(e);
    }

    public static <E> ImmutableNonEmptyList<E> of(E e, E... others)
    {
        return new ImmutableNonEmptyList<E>(e, others);
    }

    @Override
    protected List<T> delegate()
    {
        return delegate;
    }
}
