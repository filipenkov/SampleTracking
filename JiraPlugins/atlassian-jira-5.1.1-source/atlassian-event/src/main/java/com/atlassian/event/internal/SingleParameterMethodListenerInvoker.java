package com.atlassian.event.internal;

import com.atlassian.event.spi.ListenerInvoker;
import com.google.common.collect.Sets;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A listener invoker that knows how to call a given single parameter method on a given object.
 * @since 2.0
 */
final class SingleParameterMethodListenerInvoker implements ListenerInvoker
{
    private final Method method;
    private final Object listener;

    public SingleParameterMethodListenerInvoker(Object listener, Method method)
    {
        this.listener = checkNotNull(listener);
        this.method = checkNotNull(method);
    }

    public Set<Class<?>> getSupportedEventTypes()
    {
        return Sets.newHashSet(method.getParameterTypes());
    }

    public void invoke(Object event)
    {
        try
        {
            method.invoke(listener, event);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
        catch (InvocationTargetException e)
        {
            if (e.getCause() == null)
            {
                throw new RuntimeException(e);
            }
            else if (e.getCause().getMessage() == null)
            {
                throw new RuntimeException(e.getCause());
            }
            else
            {
                throw new RuntimeException(e.getCause().getMessage(), e.getCause());
            }
        }
    }

    public boolean supportAsynchronousEvents()
    {
        return true;
    }
}
