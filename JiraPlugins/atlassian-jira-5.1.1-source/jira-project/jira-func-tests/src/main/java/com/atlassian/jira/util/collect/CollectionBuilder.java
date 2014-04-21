package com.atlassian.jira.util.collect;

import net.jcip.annotations.NotThreadSafe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Convenience class for creating collections ({@link Set} and {@link List}) instances or 
 * {@link EnclosedIterable enclosed iterables}.
 * <p>
 * The default methods {@link #asList()} and {@link #asSet()} and {@link #asSortedSet()} create immutable collections.
 * 
 * @param <T> contained in the created collections.
 */
@NotThreadSafe
public final class CollectionBuilder<T>
{
    private final List<T> elements = new LinkedList<T>();

    public static <T> CollectionBuilder<T> newBuilder()
    {
        return new CollectionBuilder<T>(Collections.<T> emptyList());
    }

    public static <T> CollectionBuilder<T> newBuilder(final T... elements)
    {
        return new CollectionBuilder<T>(Arrays.asList(elements));
    }

    public static <T> CollectionBuilder<T> newBuilder(final Collection<? extends T> elements)
    {
        return new CollectionBuilder<T>(elements);
    }

    CollectionBuilder(final Collection<? extends T> initialElements)
    {
        elements.addAll(initialElements);
    }

    public CollectionBuilder<T> add(final T element)
    {
        elements.add(element);
        return this;
    }

    public <E extends T> CollectionBuilder<T> addAll(final E... elements)
    {
        this.elements.addAll(Arrays.asList(notNull("elements", elements)));
        return this;
    }

    public CollectionBuilder<T> addAll(final Collection<? extends T> elements)
    {
        this.elements.addAll(notNull("elements", elements));
        return this;
    }

    public Collection<T> asCollection()
    {
        return asList();
    }

    public Collection<T> asMutableCollection()
    {
        return asMutableList();
    }

    public List<T> asArrayList()
    {
        return new ArrayList<T>(elements);
    }

    public List<T> asLinkedList()
    {
        return new LinkedList<T>(elements);
    }

    public List<T> asList()
    {
        return Collections.unmodifiableList(new ArrayList<T>(elements));
    }

    public List<T> asMutableList()
    {
        return asArrayList();
    }

    public Set<T> asHashSet()
    {
        return new HashSet<T>(elements);
    }

    public Set<T> asListOrderedSet()
    {
        return new LinkedHashSet<T>(elements);
    }

    public Set<T> asImmutableListOrderedSet()
    {
        return Collections.unmodifiableSet(new LinkedHashSet<T>(elements));
    }

    public Set<T> asSet()
    {
        return Collections.unmodifiableSet(new HashSet<T>(elements));
    }

    public Set<T> asMutableSet()
    {
        return asHashSet();
    }

    public SortedSet<T> asTreeSet()
    {
        return new TreeSet<T>(elements);
    }

    public SortedSet<T> asSortedSet()
    {
        return Collections.unmodifiableSortedSet(new TreeSet<T>(elements));
    }

    public SortedSet<T> asMutableSortedSet()
    {
        return asTreeSet();
    }

    public EnclosedIterable<T> asEnclosedIterable()
    {
        return CollectionEnclosedIterable.copy(elements);
    }

    @Override
    public String toString()
    {
        return elements.toString();
    }
}
