/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service;

import com.atlassian.configurable.ObjectConfiguration;
import com.atlassian.configurable.ObjectConfigurationException;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.plugin.ComponentClassManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.Predicate;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import com.atlassian.util.concurrent.SettableFuture;
import com.google.common.collect.Iterables;
import com.opensymphony.module.propertyset.PropertySet;
import net.jcip.annotations.GuardedBy;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static com.atlassian.jira.util.collect.CollectionUtil.filter;
import static com.atlassian.jira.util.collect.Transformed.iterable;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

@EventComponent
public class DefaultServiceManager implements ServiceManager
{
    private static final Logger log = Logger.getLogger(DefaultServiceManager.class);

    private final ServiceConfigStore serviceConfigStore;
    // TODO: Using CopyOnWriteMap is pointless because all the operations are done under a synchronised lock anyway.
    private final Map<Long, JiraServiceContainer> services = CopyOnWriteMap.newHashMap();
    private final ServiceScheduleSkipperImpl scheduleSkipper = new ServiceScheduleSkipperImpl();
    private final ComponentClassManager componentClassManager;
    private final PermissionManager permissionManager;
    private final InBuiltServiceTypes inBuiltServiceTypes;

    public DefaultServiceManager(final ServiceConfigStore serviceConfigStore, final ComponentClassManager componentClassManager,
            final PermissionManager permissionManager, final InBuiltServiceTypes inBuiltServiceTypes)
    {
        this.permissionManager = permissionManager;
        this.inBuiltServiceTypes = inBuiltServiceTypes;
        this.serviceConfigStore = notNull("serviceConfigStore", serviceConfigStore);
        this.componentClassManager = componentClassManager;
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        refreshAll();
    }

    public synchronized Collection<JiraServiceContainer> getServices()
    {
        ensureServicesLoaded();
        return Collections.unmodifiableCollection(services.values());
    }

    @Override
    public Iterable<JiraServiceContainer> getServicesManageableBy(final User user)
    {
        final class CanManageServicePredicate implements com.google.common.base.Predicate<JiraServiceContainer>
        {
            @Override
            public boolean apply(@Nullable final JiraServiceContainer service)
            {
                if (isAnonymous(user))
                {
                    return false;
                }
                if (permissionManager.hasPermission(Permissions.SYSTEM_ADMIN, user))
                {
                    return true;
                }
                else if (permissionManager.hasPermission(Permissions.ADMINISTER, user))
                {
                    return Iterables.any(inBuiltServiceTypes.manageableBy(user), new com.google.common.base.Predicate<InBuiltServiceTypes.InBuiltServiceType>()
                    {
                        @Override
                        public boolean apply(@Nullable InBuiltServiceTypes.InBuiltServiceType anInBuiltServiceType)
                        {
                            return anInBuiltServiceType.getType().getName().equals(service.getServiceClass());
                        }
                    });
                }
                return false;
            }
        }
        return Iterables.filter(getServices(), new CanManageServicePredicate());
    }

    /* doesn't need to be synchronised as it calls getServices() */
    @Override
    public Iterable<JiraServiceContainer> getServicesForExecution(final long time)
    {
        final class ServiceDuePredicate implements Predicate<JiraServiceContainer>
        {
            public boolean evaluate(final JiraServiceContainer service)
            {
                return service.isUsable() && !service.isRunning() && (scheduleSkipper.runNow(service.getId()) || service.isDueAt(time));
            }
        }
        final class SingleShotServiceContainerTransformer
                implements Function<JiraServiceContainer, JiraServiceContainer>
        {
            public JiraServiceContainer get(final JiraServiceContainer input)
            {
                if (scheduleSkipper.runNow(input.getId()))
                {
                    return new SingleShotServiceContainer(input, input.isDueAt(time));
                }
                return input;
            }
        }
        return iterable(filter(getServices(), new ServiceDuePredicate()), new SingleShotServiceContainerTransformer());
    }

    @Override
    public synchronized boolean containsServiceWithId(final Long id)
    {
        ensureServicesLoaded();
        return services.containsKey(id);
    }

    @Override
    public void refreshAll()
    {
        loadServices();
    }

    @Override
    public synchronized JiraServiceContainer getServiceWithId(final Long id) throws Exception
    {
        ensureServicesLoaded();
        return services.get(id);
    }

    @Override
    public synchronized JiraServiceContainer getServiceWithName(final String name) throws Exception
    {
        ensureServicesLoaded();

        final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForName(name);
        if ((jiraServiceContainer != null) && services.containsKey(jiraServiceContainer.getId()))
        {
            return jiraServiceContainer;
        }
        else
        {
            return null;
        }
    }

    @Override
    public synchronized JiraServiceContainer addService(final String name, final String serviceClassName, final long delay)
            throws GenericEntityException, ServiceException, ClassNotFoundException
    {
        return addService(name, serviceClassName, delay, null);
    }

    @Override
    public synchronized JiraServiceContainer addService(final String name, final String serviceClassName, final long delay, final Map<String, String[]> params)
            throws GenericEntityException, ServiceException, ClassNotFoundException
    {
        if (StringUtils.isBlank(serviceClassName))
        {
            throw new ServiceException("The service class name must not be blank");
        }
        // Load the service class using the ComponentClassManager so we include plugins2 classes including plugins that are not enabled yet.
        final Class<JiraService> serviceClass = componentClassManager.loadClass(serviceClassName);

        return addService(name, serviceClass, delay, params);
    }

    public synchronized JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay)
            throws GenericEntityException, ServiceException
    {
        return addService(name, serviceClass, delay, null);
    }

    public synchronized JiraServiceContainer addService(String name, Class<? extends JiraService> serviceClass, long delay, Map<String, String[]> params)
            throws GenericEntityException, ServiceException
    {
        // Add the service to the map of services
        final JiraServiceContainer serviceContainer = serviceConfigStore.addServiceConfig(name, serviceClass, delay);

        if (params != null)
        {
            //update the service with the correct params
            serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
        }
        updateCache(serviceContainer);
        return serviceContainer;
    }

    public synchronized void editServiceByName(final String name, final long delay, final Map<String, String[]> params)
            throws Exception
    {
        final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForName(name);
        if (serviceContainer == null)
        {
            throw new IllegalArgumentException("There is not ServiceConfig with name: " + name);
        }
        else if (!serviceContainer.isUsable())
        {
            throw new IllegalStateException("You can not edit an unloadable service");
        }
        serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
        updateCache(serviceContainer);
    }

    public synchronized void editService(final Long id, final long delay, final Map<String, String[]> params)
            throws Exception
    {
        final JiraServiceContainer serviceContainer = serviceConfigStore.getServiceConfigForId(id);
        serviceConfigStore.editServiceConfig(serviceContainer, delay, params);
        updateCache(serviceContainer);
    }

    public synchronized void removeServiceByName(final String name) throws Exception
    {
        final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForName(name);
        if (jiraServiceContainer == null)
        {
            throw new IllegalArgumentException("No services with name '" + name + "' exist.");
        }

        serviceConfigStore.removeServiceConfig(jiraServiceContainer);
        services.remove(jiraServiceContainer.getId());
    }

    public synchronized void removeService(final Long id) throws Exception
    {
        final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForId(id);
        serviceConfigStore.removeServiceConfig(jiraServiceContainer);
        services.remove(jiraServiceContainer.getId());
    }

    public synchronized void refreshService(final Long id) throws Exception
    {
        final JiraServiceContainer newJiraServiceContainer = serviceConfigStore.getServiceConfigForId(id);
        updateCache(newJiraServiceContainer);
    }

    public synchronized void refreshServiceByName(final String name) throws Exception
    {
        final JiraServiceContainer jiraServiceContainer = serviceConfigStore.getServiceConfigForName(name);
        if (jiraServiceContainer == null)
        {
            throw new IllegalArgumentException("There is no ServiceConfig with name: " + name);
        }

        updateCache(jiraServiceContainer);
    }

    /**
     * Returns service schedule skipper
     *
     * @return service schedule skipper
     * @since v3.10
     */
    public ServiceScheduleSkipper getScheduleSkipper()
    {
        return scheduleSkipper;
    }

    /**
     * Checks if the Services are loaded already, and loads them if not.
     */
    private synchronized void ensureServicesLoaded()
    {
        // Check if it is valid to load the Services yet.
        if (!ComponentManager.getInstance().getState().isComponentsRegistered())
        {
            throw new IllegalStateException("It is illegal to call the ServiceManager before all components are loaded. Please use " + Startable.class + " to get notified when JIRA has started.");
        }

        if (services.isEmpty())
        {
            loadServices();
        }
    }

    private synchronized void loadServices()
    {
        services.clear();

        try
        {
            final Collection<JiraServiceContainer> serviceConfigs = serviceConfigStore.getAllServiceConfigs();

            if ((serviceConfigs == null) || serviceConfigs.isEmpty())
            {
                log.info("No Services to Load");
                return;
            }

            for (final JiraServiceContainer jiraServiceContainer : serviceConfigs)
            {
                services.put(jiraServiceContainer.getId(), jiraServiceContainer);
            }
        }
        catch (final Exception t)
        {
            log.error("Could not configure services: ", t);
        }
    }

    // must be called under sync
    @GuardedBy ("this")
    private void updateCache(final JiraServiceContainer jiraServiceContainer)
    {
        // If we are to put anything in the cache, then we better make sure it is loaded first.
        ensureServicesLoaded();
        services.put(jiraServiceContainer.getId(), jiraServiceContainer);
    }

    /**
     * Implementation of ServiceScheduleSkipper that has synchronized access.
     *
     * @since v3.10
     */
    protected static class ServiceScheduleSkipperImpl implements ServiceScheduleSkipper
    {
        private final ConcurrentMap<Long, SettableFuture<Void>> ids = new ConcurrentHashMap<Long, SettableFuture<Void>>();

        public boolean addService(final Long serviceId)
        {
            return ids.putIfAbsent(serviceId, new SettableFuture<Void>()) == null;
        }

        public void awaitServiceRun(final Long serviceId) throws InterruptedException
        {
            final Future<Void> future = ids.get(serviceId);
            if (future != null)
            {
                try
                {
                    future.get();
                }
                catch (final ExecutionException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }

        boolean runNow(final Long serviceId)
        {
            return ids.containsKey(serviceId);
        }

        boolean complete(final Long serviceId)
        {
            final SettableFuture<Void> oldValue = ids.remove(serviceId);
            if (oldValue == null)
            {
                return false;
            }
            oldValue.set(null);
            return true;
        }
    }

    private class SingleShotServiceContainer implements JiraServiceContainer
    {
        private final JiraServiceContainer delegate;
        private final boolean wasDueAnyway;

        private SingleShotServiceContainer(final JiraServiceContainer delegate, final boolean wasDueAnyway)
        {
            this.delegate = delegate;
            this.wasDueAnyway = wasDueAnyway;
        }

        public void run()
        {
            try
            {
                delegate.run();
            }
            finally
            {
                scheduleSkipper.complete(getId());
            }
        }

        /**
         * One off runs don't record last run
         */
        public void setLastRun()
        {
            if (wasDueAnyway)
            {
                delegate.setLastRun();
            }
        }

        public boolean isDueAt(final long time)
        {
            return delegate.isDueAt(time);
        }

        public long getLastRun()
        {
            return delegate.getLastRun();
        }

        public long getDelay()
        {
            return delegate.getDelay();
        }

        public void destroy()
        {
            delegate.destroy();
        }

        public String getDefaultProperty(final String propertyKey) throws ObjectConfigurationException
        {
            return delegate.getDefaultProperty(propertyKey);
        }

        public String getDescription()
        {
            return delegate.getDescription();
        }

        public Long getId()
        {
            return delegate.getId();
        }

        public String getKey()
        {
            return delegate.getKey();
        }

        public Long getLongProperty(final String propertyKey) throws ObjectConfigurationException
        {
            return delegate.getLongProperty(propertyKey);
        }

        public String getName()
        {
            return delegate.getName();
        }

        public ObjectConfiguration getObjectConfiguration() throws ObjectConfigurationException
        {
            return delegate.getObjectConfiguration();
        }

        public PropertySet getProperties() throws ObjectConfigurationException
        {
            return delegate.getProperties();
        }

        public String getProperty(final String propertyKey) throws ObjectConfigurationException
        {
            return delegate.getProperty(propertyKey);
        }

        public String getServiceClass()
        {
            return delegate.getServiceClass();
        }

        @Override
        public Class getServiceClassObject()
        {
            return delegate.getServiceClassObject();
        }

        public String getTextProperty(final String propertyKey) throws ObjectConfigurationException
        {
            return delegate.getTextProperty(propertyKey);
        }

        public boolean hasProperty(final String propertyKey) throws ObjectConfigurationException
        {
            return delegate.hasProperty(propertyKey);
        }

        public boolean isInternal()
        {
            return delegate.isInternal();
        }

        public boolean isRunning()
        {
            return delegate.isRunning();
        }

        public boolean isUnique()
        {
            return delegate.isUnique();
        }

        public boolean isUsable()
        {
            return delegate.isUsable();
        }

        public void setDelay(final long delay)
        {
            throw new UnsupportedOperationException();
        }

        public void setName(final String name)
        {
            throw new UnsupportedOperationException();
        }

        public void init(final PropertySet props) throws ObjectConfigurationException
        {
            throw new UnsupportedOperationException();
        }
    }

}
