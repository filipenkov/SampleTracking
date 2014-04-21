package com.atlassian.crowd.embedded.impl;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.ForwardingSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Set;

/**
 * This class behaves like a HashSet with lower-case String values. All element arguments are
 * lower-cased before further processing.
 */
public class IdentifierSet extends ForwardingSet<String>
{
    private final Set<String> delegate;

    public IdentifierSet()
    {
        delegate = Sets.newHashSet();
    }

    public IdentifierSet(int expectedSize)
    {
        delegate = Sets.newHashSetWithExpectedSize(expectedSize);
    }

    @Override
    protected Set<String> delegate()
    {
        return delegate;
    }

    private Object lowercase(Object element)
    {
        return element instanceof String ? IdentifierUtils.toLowerCase((String) element) : element;
    }

    private Collection<?> lowercase(Collection<?> collection)
    {
        return Collections2.transform(collection, new Function<Object, Object>() {

            @Override
            public Object apply(Object element)
            {
                return lowercase(element);
            }
        });
    }

    @Override
    public boolean removeAll(Collection<?> collection)
    {
        return delegate().removeAll(lowercase(collection));
    }

    @Override
    public boolean contains(Object object)
    {
        return delegate().contains(lowercase(object));
    }

    @Override
    public boolean add(String element)
    {
        return delegate().add((String) lowercase(element));
    }

    @Override
    public boolean remove(Object object)
    {
        return delegate().remove(lowercase(object));
    }

    @Override
    public boolean containsAll(Collection<?> collection)
    {
        return delegate().containsAll(lowercase(collection));
    }

    @Override
    public boolean addAll(Collection<? extends String> strings)
    {
        return delegate().addAll((Collection <? extends String >) lowercase(strings));
    }

    @Override
    public boolean retainAll(Collection<?> collection)
    {
        return delegate().retainAll(lowercase(collection));
    }
}
