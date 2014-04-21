package com.atlassian.applinks.host;

import com.atlassian.plugin.osgi.container.OsgiContainerManager;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import org.osgi.util.tracker.ServiceTracker;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Creates a proxy for an osgi service that is able to link to different implementations at runtime.  Limited to
 * one interface.
 *
 * @since 3.0
 */
public class OsgiServiceProxyFactory
{
    private final Map<Class<?>, ServiceTracker> serviceTrackers;

    /**
     * Abstracts how service trackers are created, mostly for testing
     */
    public static interface ServiceTrackerFactory
    {
        ServiceTracker create(String name);
    }

    /**
     * Thrown if the timeout waiting for the service has been exceeded
     */
    public static class ServiceTimeoutExceeded extends RuntimeException
    {
        public ServiceTimeoutExceeded(final String message)
        {
            super(message);
        }
    }

    /**
     * Constructs a service proxy factory, using the OsgiContainerManager's service tracker
     *
     * @param osgiContainerManager The osgi container manager
     */
    public OsgiServiceProxyFactory(final OsgiContainerManager osgiContainerManager)
    {
        this(new ServiceTrackerFactory()
        {
            public ServiceTracker create(final String name)
            {
                return osgiContainerManager.getServiceTracker(name);
            }
        });

    }

    public OsgiServiceProxyFactory(final ServiceTrackerFactory serviceTrackerFactory)
    {
        serviceTrackers = new MapMaker()
                .makeComputingMap(
                        new Function<Class, ServiceTracker>()
                        {
                            public ServiceTracker apply(final Class key)
                            {
                                return serviceTrackerFactory.create(key.getName());
                            }
                        });
    }

    @SuppressWarnings("unchecked")
    public <T> T createProxy(final Class<T> apiClass, final long timeoutInMillis)
    {
        // we use the bundleContext's classloader since it was loaded from the main webapp
        return (T) Proxy.newProxyInstance(apiClass.getClassLoader(), new Class[]{apiClass},
                new DynamicServiceInvocationHandler(serviceTrackers, apiClass, timeoutInMillis));
    }

    /**
     * InvocationHandler for a dynamic proxy that ensures all methods are executed with the
     * object class's class loader as the context class loader.
     */
    static class DynamicServiceInvocationHandler implements InvocationHandler
    {
        private final Map<Class<?>, ServiceTracker> serviceTrackers;
        private final Class clazz;
        private final long timeoutInMillis;

        DynamicServiceInvocationHandler(final Map<Class<?>, ServiceTracker> serviceTrackers, final Class clazz, final long timeoutInMillis)
        {
            this.serviceTrackers = serviceTrackers;
            this.clazz = clazz;
            this.timeoutInMillis = timeoutInMillis;
        }

        public Object invoke(final Object o, final Method method, final Object[] objects) throws Throwable
        {
            final ClassLoader oldContextClassLoader = Thread.currentThread().getContextClassLoader();
            try
            {
                final Object service = serviceTrackers.get(clazz).waitForService(timeoutInMillis);
                if (service == null)
                {
                    throw new ServiceTimeoutExceeded("Timeout exceeded waiting for service - " + clazz.getName());
                }
                else
                {
                    Thread.currentThread().setContextClassLoader(service.getClass().getClassLoader());
                    return method.invoke(service, objects);
                }
            }
            catch (final InvocationTargetException e)
            {
                throw e.getTargetException();
            }
            finally
            {
                Thread.currentThread().setContextClassLoader(oldContextClassLoader);
            }
        }
    }

}
