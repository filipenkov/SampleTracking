package com.atlassian.plugins.rest.module.util;

import com.atlassian.plugins.rest.module.ChainingClassLoader;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.Factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Proxy creation utilities
 */
public class ProxyUtils
{
    private static Map<Class<?>, ConstructorAndArgs> generatorCache = new MapMaker().weakKeys().makeComputingMap(new Function<Class<?>, ConstructorAndArgs>()
    {
        public ConstructorAndArgs apply(Class<?> from)
        {
            return new ConstructorAndArgs(from);
        }
    });

    public static <T> T create(Class<T> clazz, Callback callback)
    {
        return (T) generatorCache.get(clazz).create(callback);
    }
}

/*
This class encapsulates a proxy and it's construction args. These should be cached per class.
 */

class ConstructorAndArgs
{
    private Class<?> clazz;
    private Object prototype;
    private Object[] args;
    private Constructor<?> constructor;

    ConstructorAndArgs(Class<?> clazz)
    {
        this.clazz = clazz;
        initialise();
    }

    private void initialise()
    {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(new UnsupportedOperationInvocationHandler());
        enhancer.setClassLoader(new ChainingClassLoader(ProxyUtils.class.getClassLoader(), clazz.getClassLoader()));

        Constructor<?>[] constructors = clazz.getConstructors();

        for (Constructor constructor : constructors)
        {
            if ((constructor.getModifiers() & Modifier.PUBLIC) != 0)
            {
                this.constructor = constructor;

                int size = constructor.getParameterTypes().length;
                args = new Object[size];

                for (int i = 0; i < args.length; i++)
                    args[i] = createEmptyValue(constructor.getParameterTypes()[i]);

                prototype = clazz.cast(enhancer.create(constructor.getParameterTypes(), args));
                return;
            }
        }

        throw new IllegalArgumentException("Class has no accessible constructor");
    }


    private static Object createEmptyValue(Class aClass)
    {
        //todo: add more types
        if (aClass.isInterface())
            return stubInterface(aClass);
        else if (aClass == Long.TYPE)
            return 0L;
        else
            return null;
    }

    private static Object stubInterface(Class _interface)
    {
        return Proxy.newProxyInstance(_interface.getClassLoader(), new Class[]{_interface}, UnsupportedOperationInvocationHandler.INSTANCE);
    }

    public Object create(Callback... callback)
    {
        return clazz.cast(((Factory) prototype).newInstance(constructor.getParameterTypes(), args, callback));
    }
}

class UnsupportedOperationInvocationHandler implements InvocationHandler, net.sf.cglib.proxy.InvocationHandler
{
    public static UnsupportedOperationInvocationHandler INSTANCE = new UnsupportedOperationInvocationHandler();

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        throw new UnsupportedOperationException();
    }
}
