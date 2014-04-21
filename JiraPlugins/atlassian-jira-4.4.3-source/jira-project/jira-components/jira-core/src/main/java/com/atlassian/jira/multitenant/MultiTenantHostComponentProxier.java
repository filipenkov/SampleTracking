package com.atlassian.jira.multitenant;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.plugin.JiraHostContainer;
import com.atlassian.jira.plugin.JiraModuleFactory;
import com.atlassian.jira.plugin.JiraOsgiContainerManager;
import com.atlassian.jira.plugin.JiraPluginManager;
import com.atlassian.multitenant.MultiTenantComponentFactory;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantManager;
import com.atlassian.multitenant.TenantReference;
import com.atlassian.multitenant.impl.MultiTenantComponentFactoryImpl;
import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
import com.atlassian.plugin.osgi.hostcomponents.PropertyBuilder;
import com.atlassian.plugin.servlet.DefaultServletModuleManager;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxies components provided to the plugins system from JIRA, and adds new instances to the proxies as new tenants
 * are brought up/down.
 *
 * @since v4.3
 */
public class MultiTenantHostComponentProxier
{
    private static final List<String> EXCLUDES = Arrays.asList(
            "com.atlassian.multitenant",
            "com.atlassian.jira.multitenant",
            JiraPluginManager.class.getName(),
            JiraHostContainer.class.getName(),
            JiraModuleFactory.class.getName(),
            JiraOsgiContainerManager.class.getName(),
            DefaultServletModuleManager.class.getName(),
            DefaultPluginEventManager.class.getName()
            );
    private Map<String, MultiTenantComponentMap<Object>> proxyMap = CopyOnWriteMap.<String, MultiTenantComponentMap<Object>>builder().newHashMap();
    private final MultiTenantComponentFactory factory;
    private final TenantReference tenantReference;
    private final MultiTenantManager multiTenantManager;
    private final EventPublisher eventPublisher;

    public MultiTenantHostComponentProxier(MultiTenantComponentFactory factory, TenantReference tenantReference,
            MultiTenantManager multiTenantManager, PluginsEventPublisher eventPublisher)
    {
        this.factory = factory;
        this.tenantReference = tenantReference;
        this.multiTenantManager = multiTenantManager;
        this.eventPublisher = eventPublisher;
        eventPublisher.register(this);
    }

    public void addToRegistry(List<HostComponentRegistration> registry, ComponentRegistrar registrar)
    {
        // Convert all the instances to proxied instances if they aren't already
        Map<String, MultiTenantComponentMap<Object>> tmpMap = new HashMap<String, MultiTenantComponentMap<Object>>();
        for (HostComponentRegistration registration : registry)
        {
            String name = registration.getProperties().get(PropertyBuilder.BEAN_NAME);
            if (name == null)
            {
                String genKey = String.valueOf(Arrays.asList(registration.getMainInterfaces()).hashCode());
                registration.getProperties().put(PropertyBuilder.BEAN_NAME, "hostComponent-" + genKey);
            }

            boolean excluded = false;
            for (String exclude : EXCLUDES)
            {
                if (registration.getInstance().getClass().getName().startsWith(exclude))
                {
                    // Don't proxy plugins classes
                    if (registrar != null)
                    {
                        register(registrar, registration, registration.getInstance());
                    }
                    excluded = true;
                    break;
                }
            }
            if (!excluded)
            {
                if (proxyMap.containsKey(name))
                {
                    // Register this instance
                    proxyMap.get(name).addInstance(registration.getInstance());
                }
                else if (registrar == null)
                {
                    throw new IllegalStateException("A new component has been registered for this tenant but there's no registrar to register it with: " + name);
                }
                else
                {
                    MultiTenantComponentMap<Object> map = factory.createComponentMap(null);
                    map.addInstance(registration.getInstance());
                    // Create a new proxy, and put it in the tmpMap
                    tmpMap.put(name, map);
                    Object proxy = Proxy.newProxyInstance(this.getClass().getClassLoader(),
                            registration.getMainInterfaceClasses(),
                            new MultiTenantComponentFactoryImpl.MultiTenantAwareInvocationHandler<Object>(map,
                                    tenantReference, multiTenantManager));
                    register(registrar, registration, proxy);
                }
            }
        }
        proxyMap.putAll(tmpMap);
    }

    private void register(ComponentRegistrar registrar, HostComponentRegistration registration, Object instance)
    {
        PropertyBuilder builder = registrar.register(registration.getMainInterfaceClasses()).forInstance(instance);
        Enumeration<String> propKeys = registration.getProperties().keys();
        while (propKeys.hasMoreElements())
        {
            String key = propKeys.nextElement();
            builder.withProperty(key, registration.getProperties().get(key));
        }
    }

    @EventListener
    public void destroy(PluginFrameworkShutdownEvent event)
    {
        for (MultiTenantComponentMap<?> map : proxyMap.values())
        {
            map.destroy();
        }
        proxyMap.clear();
        eventPublisher.unregister(this);
    }

}
