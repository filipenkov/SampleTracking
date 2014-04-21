package com.atlassian.core.test.util;

import java.lang.reflect.*;
import java.util.*;

/**
 * Utility for getting a proxy that delegates to a list of delegates in order. The delegates are queried whether they
 * implement a particular method and if so, it is called and the result returned. If they don't, the next handler is
 * queried and so forth. If none of the objects implement the interface then the UnimplementedMethodhandler is invoked.
 * None of the delegates need to actually implement the proxied interface, only have a method with the same signature.
 * <p>
 * So for instance, given:
 *
 * <pre>
 * interface MyInterface
 * {
 *     String getString();
 *
 *     Long getLong();
 *
 *     Integer getInteger();
 * }
 * </pre>
 *
 * you can create a proxy for this interface without needing to implement the whole thing:
 *
 * <pre>
 *      Object mocker = new Object()
 *      {
 *          public String myMethod()
 *          {
 *              return &quot;proxied&quot;;
 *          }
 *      }
 *      MyInterface mock = (MyInterface) DuckTypeProxy.getProxy(MyInterface.class, mocker);
 *      System.out.println(mock.getString());
 * </pre>
 *
 * prints "proxied" to the console.
 * <p>
 * There are {@link UnimplementedMethodHandler facilities for handling unimplemented methods} either by
 * {@link #RETURN_NULL returning null} or {@link #THROW throwing exceptions by default}.
 */
public class DuckTypeProxy
{
    // -------------------------------------------------------------------------------------------------- static members

    /**
     * Return null if the method cannot be found.
     */
    public static UnimplementedMethodHandler RETURN_NULL = new UnimplementedMethodHandler()
    {
        public Object methodNotImplemented(Method method, Object[] args)
        {
            return null;
        }
    };

    /**
     * Throw an exception if the method cannot be found.
     */
    public static UnimplementedMethodHandler THROW = new UnimplementedMethodHandler()
    {
        public Object methodNotImplemented(Method method, Object[] args)
        {
            throw new UnsupportedOperationException(method.toString());
        }
    };

    // ------------------------------------------------------------------------------------------------- factory methods

    /**
     * Get a Proxy that checks each of the enclosed objects and calls them if they have a Method of the same signature.
     * By default this will {@link #THROW throw an UnsupportedOperationException} if the method is not implemented
     */
    /* <T> */public static/* T */Object getProxy(/* Class<T> */Class implementingClass, List delegates)
    {
        return getProxy(new Class[] { implementingClass }, delegates);
    }

    /**
     * Get a Proxy that checks each of the enclosed objects and calls them if they have a Method of the same signature.
     */
    /* <T> */public static/* T */Object getProxy(/* Class<T> */Class implementingClass, List delegates,
                                                 UnimplementedMethodHandler unimplementedMethodHandler)
    {
        return getProxy(new Class[] { implementingClass }, delegates, unimplementedMethodHandler);
    }

    /**
     * Get a Proxy that checks each of the enclosed objects and calls them if they have a Method of the same signature.
     * Uses the {@link #THROW} {@link UnimplementedMethodHandler}
     */
    public static Object getProxy(Class[] implementingClasses, List delegates)
    {
        return getProxy(implementingClasses, delegates, THROW);
    }

    public static Object getProxy(Class[] implementingClasses, List delegates, UnimplementedMethodHandler unimplementedMethodHandler)
    {
        return Proxy.newProxyInstance(DuckTypeProxy.class.getClassLoader(), implementingClasses, new DuckTypeInvocationHandler(delegates,
            unimplementedMethodHandler));
    }

    /**
     * Get a Proxy that checks the enclosed object and calls it if it has a Method of the same signature.
     * By default this will {@link #THROW throw an UnsupportedOperationException} if the method is not implemented
     */
    /* <T> */public static/* T */Object getProxy(/* Class<T> */Class implementingClass, Object delegate)
    {
        return getProxy(new Class[] { implementingClass }, Arrays.asList(new Object[] {delegate}));
    }

    // --------------------------------------------------------------------------------------------------- inner classes

    public interface UnimplementedMethodHandler
    {
        Object methodNotImplemented(Method method, Object[] args);
    }

    /**
     * The invocation handler that keeps the references to the delegate objects.
     */
    private static class DuckTypeInvocationHandler implements InvocationHandler
    {
        private final List delegates;
        private final UnimplementedMethodHandler unimplementedMethodHandler;

        DuckTypeInvocationHandler(List handlers, UnimplementedMethodHandler unimplementedMethodHandler)
        {
            this.delegates = Collections.unmodifiableList(new ArrayList(handlers));
            this.unimplementedMethodHandler = unimplementedMethodHandler;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            for (Iterator it = delegates.iterator(); it.hasNext();)
            {
                Object handler = it.next();
                Method duckTypeMethod;
                try
                {
                    duckTypeMethod = handler.getClass().getMethod(method.getName(), method.getParameterTypes());
                }
                catch (NoSuchMethodException ignoreAndTryNext)
                {
                    continue;
                }
                try
                {
                    duckTypeMethod.setAccessible(true);
                    return duckTypeMethod.invoke(handler, args);
                }
                catch (IllegalArgumentException ignoreAndContinue)
                {
                    // ignored
                }
                catch (IllegalAccessException ignoreAndContinue)
                {
                    // ignored
                }
                catch (InvocationTargetException e)
                {
                    throw e.getCause();
                }
            }
            return unimplementedMethodHandler.methodNotImplemented(method, args);
        }
    }
}