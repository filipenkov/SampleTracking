package com.atlassian.jira.startup;

import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.impl.TenantComponentMap;
import com.atlassian.util.concurrent.CopyOnWriteMap;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @since v4.4
 */
public class SystemTenant implements TenantComponentMap
{
    private final Map<MultiTenantComponentMap, Object> objectMap = CopyOnWriteMap.<MultiTenantComponentMap, Object>builder().newHashMap();
    private final Map<Class<?>, Object> configMap;
    private final SystemTenantJiraHomeLocator jiraHomeLocator = new SystemTenantJiraHomeLocator();

    SystemTenant(Map<Class<?>, Object> configMap)
    {
        this.configMap = CopyOnWriteMap.<Class<?>, Object>builder().addAll(configMap).newHashMap();
    }

    @Override
    public <T> void putObject(final MultiTenantComponentMap<T> key, final T value)
    {
        objectMap.put(key, value);
    }

    @Override
    public boolean hasObject(final MultiTenantComponentMap key)
    {
        return objectMap.containsKey(key);
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public <T> T getObject(final MultiTenantComponentMap<T> key)
    {
        return (T) objectMap.get(key);
    }

    @SuppressWarnings ({ "unchecked" })
    @Override
    public <T> T removeObject(final MultiTenantComponentMap<T> key)
    {
        return (T) objectMap.remove(key);
    }

    @Override
    public String getName()
    {
        return SystemTenantProvider.SYSTEM_TENANT_NAME;
    }

    @Override
    public String getHomeDir()
    {
        return jiraHomeLocator.getJiraHome();
    }

    @Override
    public Collection<String> getHostnames()
    {
        return Collections.singleton("localhost");
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <B> B getConfig(Class<B> beanClazz)
    {
        return (B) configMap.get(beanClazz);
    }

    public void putConfig(Class configClass, Object configObject)
    {
        configMap.put(configClass, configObject);
    }
}
