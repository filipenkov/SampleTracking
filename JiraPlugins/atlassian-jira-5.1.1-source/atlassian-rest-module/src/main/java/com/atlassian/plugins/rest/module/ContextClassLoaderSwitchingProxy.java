package com.atlassian.plugins.rest.module;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ContextClassLoaderSwitchingProxy implements InvocationHandler
{
    private final Object delegate;
    private final ClassLoader[] classLoaders;

    public ContextClassLoaderSwitchingProxy(final Object delegate, final ClassLoader... classLoaders)
    {
        this.delegate = delegate;
        this.classLoaders = classLoaders;
    }

    public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable
    {
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        ChainingClassLoader chainingClassLoader = new ChainingClassLoader(classLoaders);
        try
        {
            try
            {
                Thread.currentThread().setContextClassLoader(chainingClassLoader);
                return method.invoke(delegate, args);
            }
            catch (InvocationTargetException e)
            {
                throw e.getCause();
            }
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(oldClassLoader);
        }

    }

}