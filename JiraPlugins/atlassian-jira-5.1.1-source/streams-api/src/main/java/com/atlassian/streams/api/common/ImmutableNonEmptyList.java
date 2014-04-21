package com.atlassian.streams.api.common;

import java.util.List;

import com.google.common.collect.ForwardingList;
import com.google.common.collect.ImmutableList;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.Arrays.asList;

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

    public ImmutableNonEmptyList(NonEmptyIterable<T> items)
    {
        checkNotNull(items, "items");
        this.delegate = ImmutableList.copyOf(items);
    }
    
    public static <E> ImmutableNonEmptyList<E> of(E e)
    {
        return new ImmutableNonEmptyList<E>(e);
    }

    public static <E> ImmutableNonEmptyList<E> of(E e1, E e2)
    {
        return new ImmutableNonEmptyList<E>(e1, ImmutableList.of(e2));
    }

    public static <E> ImmutableNonEmptyList<E> of(E e, E... others)
    {
        return new ImmutableNonEmptyList<E>(e, asList(others));
    }

    public static <E> ImmutableNonEmptyList<E> of(E e, Iterable<E> others)
    {
        return new ImmutableNonEmptyList<E>(e, others);
    }

    public static <E> ImmutableNonEmptyList<E> copyOf(NonEmptyIterable<E> items)
    {
        return (items instanceof ImmutableNonEmptyList) ? ((ImmutableNonEmptyList<E>) items)
                : new ImmutableNonEmptyList<E>(items);
    }
    
    @Override
    protected List<T> delegate()
    {
        return delegate;
    }
}
