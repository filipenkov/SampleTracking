package com.atlassian.streams.api.common;

import java.util.concurrent.atomic.AtomicBoolean;

import com.google.common.base.Supplier;

public class Suppliers
{
    public static Supplier<Boolean> forAtomicBoolean(final AtomicBoolean a)
    {
        return new Supplier<Boolean>()
        {
            @Override
            public Boolean get()
            {
                return a.get();
            }
        };
    }
}
