package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.impl.DefaultTenant;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * This is an immutable tenant, to ensure the unit tests don't do anything stupid
 */
public class MockTenant implements Tenant
{
    private final Tenant delegate;

    public MockTenant(String name)
    {
        this(name, "homeDir", Collections.<Class<?>, Object>emptyMap());
    }

    public MockTenant(String name, String homeDir, Map<Class<?>, Object> configMap)
    {
        delegate = new DefaultTenant(name, Collections.<String>emptySet(), homeDir, Collections.unmodifiableMap(configMap));
    }

    @Override
    public String getName()
    {return delegate.getName();}

    @Override
    public Collection<String> getHostnames()
    {return delegate.getHostnames();}

    @Override
    public String getHomeDir()
    {return delegate.getHomeDir();}

    @Override
    public <B> B getConfig(Class<B> beanClazz)
    {return delegate.getConfig(beanClazz);}
}
