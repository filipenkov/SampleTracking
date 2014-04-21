package com.atlassian.plugins.rest.module;

import java.util.Collections;
import java.util.Set;

import com.atlassian.plugin.AutowireCapablePlugin;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProvider;
import com.sun.jersey.core.spi.component.ioc.IoCComponentProviderFactory;
import com.sun.jersey.core.spi.component.ioc.IoCManagedComponentProvider;
import com.sun.jersey.server.impl.container.servlet.JSPTemplateProcessor;
import com.sun.jersey.spi.resource.PerRequest;

import static com.google.common.collect.Sets.difference;

/**
 * <p>The OSGi component provider factory.</p>
 * <p>This will support <em>user defined</em> resources and providers only.</p>
 */
public class OsgiComponentProviderFactory implements IoCComponentProviderFactory
{
    // Classes that are added to the resource config that we can't autowire.
    private static final Set<Class<?>> EXCLUDE = ImmutableSet.<Class<?>>of(JSPTemplateProcessor.class);
    
    /**
     * The plugin that this factory belongs to.
     */
    private final AutowireCapablePlugin plugin;

    /**
     * The set of <em>user defined</em> resources and providers.
     */
    private final Set<Class<?>> classes;

    private final Set<?> instances;

    public OsgiComponentProviderFactory(ResourceConfig resourceConfig, AutowireCapablePlugin plugin)
    {
        this.plugin = Preconditions.checkNotNull(plugin);

        // get the "user defined" resource and provider classes
        final Set<Class<?>> classes = Preconditions.checkNotNull(resourceConfig).getClasses();
        if (classes != null)
        {
            this.classes = difference(ImmutableSet.copyOf(classes), EXCLUDE);
        }
        else
        {
            this.classes = Collections.emptySet();
        }

        if (resourceConfig instanceof OsgiResourceConfig)
        {
            instances = ((OsgiResourceConfig) resourceConfig).getInstances();
        }
        else
        {
            instances = Collections.emptySet();
        }
    }

    public IoCComponentProvider getComponentProvider(final Class<?> c)
    {
        return getComponentProvider(null, c);
    }

    public IoCComponentProvider getComponentProvider(ComponentContext cc, Class<?> c)
    {
        if (!classes.contains(c))
        {
            return null;
        }
        final Object instance = getInstance(c);
        return instance == null ? new AutowiredOsgiComponentProvider(plugin, c) : new InstanceOsgiComponentProvider(instance);
    }

    private Object getInstance(Class<?> c)
    {
        for (Object o : instances)
        {
            if (o.getClass().equals(c))
            {
                return o;
            }
        }
        return null;
    }

    private static class AutowiredOsgiComponentProvider implements IoCManagedComponentProvider
    {
        private final AutowireCapablePlugin plugin;
        private final Class<?> componentClass;

        public AutowiredOsgiComponentProvider(AutowireCapablePlugin plugin, Class<?> componentClass)
        {
            this.plugin = plugin;
            this.componentClass = componentClass;
        }

        public Object getInstance()
        {
            return plugin.autowire(componentClass);
        }

        public ComponentScope getScope()
        {
            // If the class is annotated with PerRequest, then it should return per request, otherwise,
            // default to singleton
            if (componentClass.getAnnotation(PerRequest.class) != null)
            {
                return ComponentScope.PerRequest;
            }
            return ComponentScope.Singleton;
        }

        public Object getInjectableInstance(Object o)
        {
            plugin.autowire(o);
            return o;
        }
    }

    private static class InstanceOsgiComponentProvider implements IoCManagedComponentProvider
    {
        private final Object instance;

        public InstanceOsgiComponentProvider(Object instance)
        {
            this.instance = Preconditions.checkNotNull(instance);
        }

        public ComponentScope getScope()
        {
            return ComponentScope.Singleton;
        }

        public Object getInstance()
        {
            return instance;
        }

        public Object getInjectableInstance(Object o)
        {
            return o;
        }
    }
}
