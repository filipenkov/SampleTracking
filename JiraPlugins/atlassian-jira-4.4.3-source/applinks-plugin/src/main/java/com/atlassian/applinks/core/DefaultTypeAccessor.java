package com.atlassian.applinks.core;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.auth.AuthenticationProvider;
import com.atlassian.applinks.core.plugin.AbstractAppLinksTypeModuleDescriptor;
import com.atlassian.applinks.core.plugin.ApplicationTypeModuleDescriptor;
import com.atlassian.applinks.core.plugin.AuthenticationProviderModuleDescriptor;
import com.atlassian.applinks.core.plugin.EntityTypeModuleDescriptor;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.applinks.spi.application.IdentifiableType;
import com.atlassian.applinks.spi.application.TypeId;
import com.atlassian.applinks.spi.auth.AuthenticationProviderPluginModule;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;
import com.atlassian.plugin.tracker.DefaultPluginModuleTracker;
import com.atlassian.plugin.tracker.PluginModuleTracker;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class DefaultTypeAccessor implements TypeAccessor, InternalTypeAccessor, DisposableBean
{
    private static final Logger log = LoggerFactory.getLogger(DefaultTypeAccessor.class);

    private final PluginModuleTracker<AuthenticationProviderPluginModule, AuthenticationProviderModuleDescriptor> authTracker;
    private final PluginModuleTracker<ApplicationType, ApplicationTypeModuleDescriptor> applicationTracker;
    private final PluginModuleTracker<EntityType, EntityTypeModuleDescriptor> entityTracker;

    private final TypeCache typeCache = new TypeCache();

    public DefaultTypeAccessor(final PluginAccessor pluginAccessor, final PluginEventManager eventManager)
    {
        applicationTracker = new DefaultPluginModuleTracker<ApplicationType, ApplicationTypeModuleDescriptor>(
                pluginAccessor, eventManager, ApplicationTypeModuleDescriptor.class, typeCache);
        entityTracker = new DefaultPluginModuleTracker<EntityType, EntityTypeModuleDescriptor>(pluginAccessor,
                eventManager, EntityTypeModuleDescriptor.class, typeCache);
        authTracker = new DefaultPluginModuleTracker<AuthenticationProviderPluginModule,
                AuthenticationProviderModuleDescriptor>(pluginAccessor, eventManager,
                AuthenticationProviderModuleDescriptor.class, typeCache);
    }

    @SuppressWarnings("unchecked")
    public <T extends EntityType> T getEntityType(final Class<T> typeClass)
    {
        return loadTypeOrDescendant(typeClass);
    }

    public EntityType loadEntityType(final TypeId typeId)
    {
        return typeCache.get(typeId.get());
    }

    public EntityType loadEntityType(final String typeClassName)
    {
        return typeCache.get(typeClassName);
    }

//    public Class<? extends EntityType> loadEntityTypeClass(final String className)
//    {
//        final EntityType entityType = typeCache.get(className);
//        return entityType != null ? entityType.getTypeClass() : null;
//    }
//
    @SuppressWarnings("unchecked")
    public <T extends ApplicationType> T getApplicationType(final Class<T> typeClass)
    {
        return loadTypeOrDescendant(typeClass);
    }

    private <T> T loadTypeOrDescendant(final Class<T> typeClass)
    {
        T type = typeCache.get(typeClass);
        if (type == null)
        {
            final Iterator<? extends T> types = typeCache.getAll(typeClass).iterator();
            if (types.hasNext())
            {
                type = types.next();
                if (types.hasNext())
                {
                    throw new IllegalArgumentException("Multiple implementations of " + typeClass.getName() + " installed!");
                }
            }
        }
        return type;
    }

    public ApplicationType loadApplicationType(final TypeId typeId)
    {
        return typeCache.get(typeId.get());
    }

    public ApplicationType loadApplicationType(final String typeClassName)
    {
        return typeCache.get(typeClassName);
    }

//    public Class<? extends ApplicationType> loadApplicationTypeClass(final String className)
//    {
//        final ApplicationType applicationType = typeCache.get(className);
//        return applicationType != null ? applicationType.getTypeClass() : null;
//    }
//
    @SuppressWarnings("unchecked")
    public Class<? extends AuthenticationProvider> getAuthenticationProviderClass(final String className)
    {
        return typeCache.get(className);
    }

    public Iterable<? extends EntityType> getEnabledEntityTypes()
    {
        return typeCache.getAll(EntityType.class);
    }

    public Iterable<? extends EntityType> getEnabledEntityTypesForApplicationType(final ApplicationType applicationType)
    {
        return Iterables.filter(getEnabledEntityTypes(), new Predicate<EntityType>()
        {
            public boolean apply(@Nullable EntityType input)
            {
                return input.getApplicationType().isAssignableFrom(applicationType.getClass());
            }
        });
    }

    public Iterable<? extends ApplicationType> getEnabledApplicationTypes()
    {
        return typeCache.getAll(ApplicationType.class);
    }

    public Iterable<? extends EntityType> getEntityTypesForApplicationType(final TypeId typeId)
    {
        final ApplicationType type = loadApplicationType(typeId);
        return Iterables.filter(getEnabledEntityTypes(), new Predicate<EntityType>()
        {
            public boolean apply(final EntityType input)
            {
                return input.getApplicationType().isAssignableFrom(type.getClass());
            }
        });
    }

    public void destroy() throws Exception
    {
        applicationTracker.close();
        entityTracker.close();
        authTracker.close();
    }

    private static class TypeCache implements PluginModuleTracker.Customizer
    {
        private final Lock write = new ReentrantLock();

        private final Map<String, Object> cache = CopyOnWriteMap.<String, Object>builder().newHashMap();
        private final Map<String, Set<String>> moduleClasses = new HashMap<String, Set<String>>();

        private <T> void put(final String completeModuleKey, final Class<? extends T> clazz, final T instance)
        {
            put(completeModuleKey, clazz.getName(), instance);
        }

        private void put(final String completeModuleKey, final String className, final Object instance)
        {
            try
            {
                write.lock();
                Set<String> stored = moduleClasses.get(completeModuleKey);
                if (stored == null)
                {
                    stored = new HashSet<String>();
                    moduleClasses.put(completeModuleKey, stored);
                }
                stored.add(className);
                cache.put(className, instance);
            }
            finally
            {
                write.unlock();
            }
        }

        @SuppressWarnings("unchecked")
        private <T> T get(final Class<T> clazz)
        {
            return (T) cache.get(clazz.getName());
        }

        @SuppressWarnings("unchecked")
        private <T> T get(final String className)
        {
            return (T) cache.get(className);
        }

        /**
         * @param type the type class to retrieve
         * @param <T> the type to retrieve
         * @return unique values of the specified type
         */
        private <T> Iterable<T> getAll(final Class<T> type)
        {
            return Iterables.filter(ImmutableSet.copyOf(cache.values()), type);
        }

        private void flush(final String moduleCompleteKey)
        {
            try
            {
                write.lock();
                final Set<String> stored = moduleClasses.get(moduleCompleteKey);
                if (stored != null)
                {
                    for (final String className : stored)
                    {
                        cache.remove(className);
                    }
                }
                moduleClasses.remove(moduleCompleteKey);
            }
            finally
            {
                write.unlock();
            }
        }

        public ModuleDescriptor adding(final ModuleDescriptor descriptor)
        {
            final String completeKey = descriptor.getCompleteKey();

            if (descriptor instanceof AbstractAppLinksTypeModuleDescriptor)
            {
                final AbstractAppLinksTypeModuleDescriptor typeDescriptor = (AbstractAppLinksTypeModuleDescriptor) descriptor;
                final Object moduleInstance = typeDescriptor.getModule(); // assign, as getModule() may return different instances each invocation

                if (!IdentifiableType.class.isAssignableFrom(moduleInstance.getClass())) { //todo this check should be done in the ModuleDescriptor
                    log.error(moduleInstance.getClass() + " does not implement " + IdentifiableType.class.getName() + "! This type will not be available.");
                    return descriptor;
                }

                put(completeKey, typeDescriptor.getModule().getClass(), moduleInstance);
                for (final String inyourface : (Iterable<String>) typeDescriptor.getInterfaces()) // TODO this cast shouldn't be necessary!?
                {
                    put(completeKey, inyourface, moduleInstance);
                }
                put(completeKey, ((IdentifiableType)moduleInstance).getId().get(), moduleInstance); // register against type id
            }
            else if (descriptor instanceof AuthenticationProviderModuleDescriptor)
            {
                final AuthenticationProviderPluginModule module = (AuthenticationProviderPluginModule) descriptor.getModule();
                put(completeKey, module.getAuthenticationProviderClass().getName(), module.getAuthenticationProviderClass());
            }
            return descriptor;
        }

        public void removed(final ModuleDescriptor descriptor)
        {
            final String completeKey = descriptor.getCompleteKey();
            flush(completeKey);
        }
    }
}
