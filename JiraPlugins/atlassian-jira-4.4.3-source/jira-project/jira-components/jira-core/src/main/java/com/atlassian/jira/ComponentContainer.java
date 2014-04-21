package com.atlassian.jira;

import com.atlassian.jira.config.component.ProfilingComponentAdapterFactory;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.Parameter;
import org.picocontainer.defaults.ComponentParameter;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.not;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static java.util.Arrays.asList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableSet;

/**
 * Register Components and track which ones are available to plugins.
 */
class ComponentContainer
{
    enum Scope
    {
        /**
         * Provided to Plugins2 plugins
         */
        PROVIDED
                {
                    @Override
                    Registrar get(final Registry registry)
                    {
                        return new Registrar()
                        {
                            public void register(final Class<?> klass)
                            {
                                registry.register(klass);
                            }

                            public void register(final ComponentAdapter adapter)
                            {
                                registry.register(adapter);
                            }
                        };
                    }
                },

        /**
         * Not provided to Plugins2 plugins
         */
        INTERNAL
                {
                    @Override
                    Registrar get(final Registry registry)
                    {
                        return new Registrar()
                        {
                            public void register(final Class<?> klass)
                            {
                            }

                            public void register(final ComponentAdapter adapter)
                            {
                            }
                        };
                    }
                };

        abstract Registrar get(Registry registry);

        interface Registrar
        {
            abstract void register(Class<?> klass);

            abstract void register(ComponentAdapter adapter);
        }
    }

    private final DefaultPicoContainer container = new DefaultPicoContainer(new ProfilingComponentAdapterFactory());
    private final Registry registry = new Registry();

    //
    // accessors
    //

    MutablePicoContainer getPicoContainer()
    {
        return container;
    }

    ComponentAdapter getComponentAdapter(final Class<?> key)
    {
        return container.getComponentAdapter(key);
    }

    <T> T getComponentInstance(final Class<T> key)
    {
        final ComponentAdapter adapter = getComponentAdapter(key);
        return key.cast(adapter.getComponentInstance());
    }

    HostComponentProvider getHostComponentProvider()
    {
        return new HostComponentProviderImpl(registry);
    }

    static class HostComponentProviderImpl implements HostComponentProvider
    {
        private final Registry registry;

        HostComponentProviderImpl(final ComponentContainer.Registry registry)
        {
            this.registry = registry;
        }

        public void provide(final ComponentRegistrar registrar)
        {
            //TODO: The order of the comoponents is non-deterministic
            //Better use a tree set and sort the components.
            final Set<Component> components = registry.getComponents();
            final Set<String> usedKeys = new HashSet<String>();
            for (final Component component : components)
            {
                final Class<?>[] interfaces = component.getInterfaces();
                for (final Class<?> iface : interfaces)
                {
                    final String name = extractSpringLikeBeanNameFromInterface(usedKeys, iface);
                    registrar.register(iface).forInstance(component.getInstance()).withName(name);
                    usedKeys.add(name);
                }
            }
        }
    }

    //
    // register instances
    //

    void instance(final Scope scope, final Object instance)
    {
        // cannot provide, non-interface keys not supported
        scope.get(registry).register(instance.getClass());
        container.registerComponentInstance(instance);
    }

    void instance(final Scope scope, final String key, final Object instance)
    {
        // cannot provide, String keys not supported
        scope.get(registry).register(instance.getClass());
        container.registerComponentInstance(key, instance);
    }

    <T, S extends T> void instance(final Scope scope, final Class<T> key, final S instance)
    {
        scope.get(registry).register(key);
        container.registerComponentInstance(key, instance);
    }

    //
    // register implementations
    //

    void implementation(final Scope scope, final Class<?> implementation)
    {
        // cannot provide, non-interface keys not supported
        scope.get(registry).register(implementation);
        container.registerComponentImplementation(implementation);
    }

    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation)
    {
        scope.get(registry).register(key);
        container.registerComponentImplementation(key, implementation);
    }

    /**
     * Registers the interface with a concrete implementation class using the given variable number of arguments as the
     * keys PICO will use to look up during instantiation.  ComponentParameters are created for each parameterKeys
     * object if its NOT already a {@link Parameter}.
     *
     * @param scope the container scope
     * @param key the interface to register
     * @param implementation the concrete implementation of interfaceClass
     * @param parameterKeys the variable number of parameters
     */
    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Object... parameterKeys)
    {
        final Parameter[] componentParameters = new Parameter[parameterKeys.length];
        for (int i = 0; i < parameterKeys.length; i++)
        {
            Object parameter = parameterKeys[i];
            if (parameter instanceof Parameter)
            {
                componentParameters[i] = (Parameter) parameter;
            }
            else
            {
                componentParameters[i] = new ComponentParameter(parameter);
            }
        }
        implementation(scope,key,implementation,componentParameters);
    }

    <T> void implementation(final Scope scope, final Class<? super T> key, final Class<T> implementation, final Parameter[] parameters)
    {
        scope.get(registry).register(key);
        container.registerComponentImplementation(key, implementation, parameters);
    }

    /*
     * Special method that registers the default constructor. Workaround for http://jira.codehaus.org/browse/PICO-201
     */

    <T> void implementationUseDefaultConstructor(final Scope scope, final Class<T> key, final Class<? extends T> implementation)
    {
        scope.get(registry).register(key);
        // we need to parameterise this, otherwise it tries to load the greediest constructor
        container.registerComponentImplementation(key, implementation, Collections.EMPTY_LIST);
    }

    void transfer(ComponentManager from, ComponentContainer.Scope scope, Class<?> key)
    {
        // Don't ask me why these casts are needed, IDEA said it compiled fine, javac begged to differ
        instance(scope, (Class<Object>) key, (Object) key.cast(from.getContainer().getComponentInstance(key)));
    }


    //
    // direct component registration
    //

    void component(final Scope scope, final ComponentAdapter componentAdapter)
    {
        // needs to do some smarts to work out if available
        scope.get(registry).register(componentAdapter);
        container.registerComponent(componentAdapter);
    }

    //
    // utility
    //

    /**
     * Determines unique spring like bean name from a set of interfaces.  Bean name generated by taking the
     * alphabetically first class name and lower-casing it.  Any duplicates will have numeric postfixes, starting with 1.
     * @param usedKeys A set of bean names already used
     * @param classes A list of classes to use to generate the bean name
     * @return A unique bean name that will never be null
     */
    static String extractSpringLikeBeanNameFromInterface(final Set<String> usedKeys, final Class<?>... classes)
    {
        notNull("classes", classes);
        not("classes must not be empty", classes.length < 1);
        final List<Class<?>> sorted = new ArrayList<Class<?>>(asList(classes));
        sort(sorted, new SimpleClassNameComparator());
        final Class<?> clazz = sorted.get(0);
        final String componentKey = clazz.getSimpleName();
        final String lowerCaseKey = componentKey.toLowerCase(Locale.ENGLISH);

        // determine unique key by attaching a numbered postfix
        int postfix = 1;
        final String baseKey = lowerCaseKey.substring(0, 1) + componentKey.substring(1);
        String key = baseKey;
        while (usedKeys.contains(key))
        {
            key = baseKey + postfix++;
        }

        return key;
    }

    /**
     * maintain the provided Components.
     */
    private class Registry
    {
        private final Set<Class<?>> availableComponents = new HashSet<Class<?>>();

        void register(final Class<?> componentKey)
        {
            if (!componentKey.isInterface())
            {
                throw new IllegalArgumentException(componentKey + " must be an interface to provide to plugins.");
            }
            if (availableComponents.contains(componentKey))
            {
                throw new IllegalArgumentException(componentKey + " has already been provided.");
            }
            availableComponents.add(componentKey);
        }

        void register(final ComponentAdapter componentAdapter)
        {
            final Object key = componentAdapter.getComponentKey();
            if (key instanceof Class<?>)
            {
                register((Class<?>) key);
            }
        }

        Set<Component> getComponents()
        {
            final MultiMap<Object, Class<?>, Set<Class<?>>> instances = MultiMaps.create(new Supplier<Set<Class<?>>>()
            {
                public Set<Class<?>> get()
                {
                    return new HashSet<Class<?>>();
                }
            });
            for (final Class<?> exposedInterface : availableComponents)
            {
                final Object instance = getComponentInstance(exposedInterface);
                if (instance != null)
                {
                    instances.put(instance, exposedInterface);
                }
            }
            final Set<Component> result = new HashSet<Component>();
            for (final Map.Entry<Object, Set<Class<?>>> entry : instances.entrySet())
            {
                result.add(new Component(entry.getKey(), entry.getValue()));
            }
            return Collections.unmodifiableSet(result);
        }
    }

    private static final class Component
    {
        private final Object instance;
        private final Set<Class<?>> interfaces;

        Component(final Object instance, final Set<Class<?>> interfaces)
        {
            this.instance = notNull("instance", instance);
            this.interfaces = unmodifiableSet(new HashSet<Class<?>>(notNull("interfaces", interfaces)));
        }

        /**
         * The actual component instance.
         * @return the instance
         */
        public Object getInstance()
        {
            return instance;
        }

        /**
         * The interfaces exported by this component.
         * @return the interfaces
         */
        public Class<?>[] getInterfaces()
        {
            return interfaces.toArray(new Class[interfaces.size()]);
        }

        @Override
        public int hashCode()
        {
            return instance.hashCode();
        }

        @Override
        public boolean equals(final Object obj)
        {
            if (obj instanceof Component)
            {
                return instance == ((Component) obj).instance;
            }
            else
            {
                return false;
            }
        }
    }

    /**
     * Compares classes by their simple name.
     * <p>
     * Needs to be static as it is held for 
     */
    static class SimpleClassNameComparator implements Comparator<Class<?>>
    {
        public int compare(final Class<?> o1, final Class<?> o2)
        {
            return o1.getSimpleName().compareToIgnoreCase(o2.getSimpleName());
        }
    }
}
