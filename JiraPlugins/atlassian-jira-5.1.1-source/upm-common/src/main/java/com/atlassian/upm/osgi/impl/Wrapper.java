package com.atlassian.upm.osgi.impl;

import java.util.Collection;

import javax.annotation.Nullable;

/**
 * A generic helper class for transforming raw OSGi types to wrapped
 * types and REST representations, and for transforming arrays and iterables
 * of those types.  Subclass and implement wrap() to use.
 *
 * @param <In> transformation input type
 * @param <Out> transformation output type
 */
public abstract class Wrapper<In, Out>
{
    private final Wrapper2<Void, In, Out> wrapper2;

    public Wrapper(String name)
    {
        this.wrapper2 = new Wrapper2<Void, In, Out>(name)
        {
            protected Out wrap(@Nullable Void key, @Nullable In value)
            {
                return Wrapper.this.wrap(value);
            }
        };
    }

    protected abstract Out wrap(@Nullable In in);

    public final Out fromSingleton(@Nullable In in)
    {
        return wrapper2.fromSingleton(null, in);
    }

    public final Iterable<Out> fromArray(@Nullable In[] in)
    {
        return wrapper2.fromArray(null, in);
    }

    public final Collection<Out> fromIterable(@Nullable Iterable<In> in)
    {
        return wrapper2.fromIterable(null, in);
    }
}
