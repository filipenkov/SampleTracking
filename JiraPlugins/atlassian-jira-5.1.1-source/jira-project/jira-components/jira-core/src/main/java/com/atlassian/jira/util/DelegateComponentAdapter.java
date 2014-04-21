package com.atlassian.jira.util;

import com.atlassian.jira.util.dbc.Assertions;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVerificationException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Used to register a single class instance against multiple keys in a {@link PicoContainer}.
 *
 * @param <T> the concrete class.
 */
public class DelegateComponentAdapter<T> implements ComponentAdapter
{
    private final Class<? super T> key;
    private final ComponentAdapter delegate;

    DelegateComponentAdapter(final @NotNull Class<? super T> key, final @NotNull ComponentAdapter delegate)
    {
        this.key = notNull("key", key);
        this.delegate = notNull("delegate", delegate);
    }

    public Class<? extends T> getComponentImplementation()
    {
        @SuppressWarnings("unchecked")
        final Class<? extends T> componentImplementation = delegate.getComponentImplementation();
        return componentImplementation;
    }

    public Class<? super T> getComponentKey()
    {
        return key;
    }

    public T getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
    {
        @SuppressWarnings("unchecked")
        final T componentInstance = (T) delegate.getComponentInstance();
        return componentInstance;
    }

    public PicoContainer getContainer()
    {
        return delegate.getContainer();
    }

    public void setContainer(final PicoContainer picoContainer)
    {
        delegate.setContainer(picoContainer);
    }

    public void verify() throws PicoVerificationException
    {
        delegate.verify();
    }

    /**
     * Builder for easily creating and registering {@link DelegateComponentAdapter}
     *
     * @param <T> the actual class we build for.
     */
    public static class Builder<T>
    {
        public static <T> Builder<T> builderFor(final @NotNull Class<T> concrete)
        {
            return new Builder<T>(concrete);
        }

        private final List<Class<? super T>> implementing = new ArrayList<Class<? super T>>();
        private final Class<T> concrete;

        Builder(final @NotNull Class<T> concrete)
        {
            this.concrete = notNull("concrete", concrete);
        }

        public Builder<T> implementing(final Class<? super T> interfaceClass)
        {
            implementing.add(interfaceClass);
            return this;
        }

        public void registerWith(final MutablePicoContainer container)
        {
            Assertions.stateTrue("must implement some interfaces", !implementing.isEmpty());
            final Iterator<Class<? super T>> interfaces = implementing.iterator();
            final Class<? super T> registered = interfaces.next();
            container.registerComponentImplementation(registered, concrete);
            while (interfaces.hasNext())
            {
                final Class<? super T> alternate = interfaces.next();
                container.registerComponent(new DelegateComponentAdapter<T>(alternate, container.getComponentAdapter(registered)));
            }
        }
    }
}
