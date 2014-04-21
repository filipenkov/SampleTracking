package com.atlassian.applinks.core.util;

/**
 * Generic holder class
 */
public class Holder<T>
{
    private T value;

    public Holder()
    {
        value = null;
    }

    public void set(final T value)
    {
        this.value = value;
    }

    public T get()
    {
        return value;
    }

    public Holder(final T value)
    {
        this.value = value;
    }
}
