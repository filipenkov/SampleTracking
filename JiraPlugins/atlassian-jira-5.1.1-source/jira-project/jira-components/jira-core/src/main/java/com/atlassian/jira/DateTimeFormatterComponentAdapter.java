package com.atlassian.jira;

import com.atlassian.jira.config.component.AbstractComponentAdaptor;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.util.concurrent.LazyReference;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.defaults.UnsatisfiableDependenciesException;

import java.lang.reflect.Method;
import java.util.Collections;

/**
 * A component adapter that creates DateTimeFormatter proxies. Whenever a method is called on the proxy, it will call
 * {@link com.atlassian.jira.datetime.DateTimeFormatterFactory#formatter()} to obtain a real DateTimeFormatter, and will
 * delegate the call to the real implementation.
 *
 * @see com.atlassian.jira.datetime.DateTimeFormatterFactory#formatter()
 * @since v5.0
 */
public class DateTimeFormatterComponentAdapter extends AbstractComponentAdaptor<DateTimeFormatter>
{
    /**
     * Lazily-initialised reference to the DateTimeFormatter proxy.
     */
    private final LazyReference<DateTimeFormatter> proxyRef = new CglibProxyCreator();

    public DateTimeFormatterComponentAdapter()
    {
        super(DateTimeFormatter.class);
    }

    @Override
    public Class<?> getComponentImplementation()
    {
        return DateTimeFormatter.class;
    }

    @Override
    public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
    {
        return proxyRef.get();
    }

    @Override
    public void verify() throws UnsatisfiableDependenciesException
    {
        // throws exception if factory is not found
        getFactoryFromContainer();
    }

    /**
     * Returns a DateTimeFormatterFactory instance from the container.
     *
     * @return a DateTimeFormatterFactory
     */
    protected DateTimeFormatterFactory getFactoryFromContainer()
    {
        DateTimeFormatterFactory factory = (DateTimeFormatterFactory) getContainer().getComponentInstanceOfType(DateTimeFormatterFactory.class);
        if (factory == null)
        {
            throw new UnsatisfiableDependenciesException(this, Collections.singleton(DateTimeFormatterFactory.class));
        }

        return factory;
    }

    /**
     * Creates a cglib proxy for DateTimeFormatter.
     */
    class CglibProxyCreator extends LazyReference<DateTimeFormatter>
    {
        @Override
        protected DateTimeFormatter create() throws Exception
        {
            DateTimeFormatterFactory factory = getFactoryFromContainer();

            // place a cglib proxy in the container
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(DateTimeFormatter.class);
            enhancer.setCallback(new DelegateToDateTimeFormatterInterceptor(factory));

            return (DateTimeFormatter) enhancer.create();
        }
    }

    /**
     * Cglib interceptor that calls the <em>real</em> DateTimeFormatter, gotten from the DateTimeFormatterFactory.
     */
    static class DelegateToDateTimeFormatterInterceptor implements MethodInterceptor
    {
        private final DateTimeFormatterFactory factory;

        public DelegateToDateTimeFormatterInterceptor(DateTimeFormatterFactory factory)
        {
            this.factory = factory;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable
        {
            DateTimeFormatter delegate = factory.formatter();

            return proxy.invoke(delegate, args);
        }
    }
}
