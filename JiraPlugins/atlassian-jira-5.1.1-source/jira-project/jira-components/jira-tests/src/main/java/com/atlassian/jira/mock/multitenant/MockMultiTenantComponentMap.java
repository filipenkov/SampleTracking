package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;

import java.util.Collection;
import java.util.Collections;

/**
 * Map that doesn't use tenant reference, rather it always returns the same component.
 */
public class MockMultiTenantComponentMap<T> implements MultiTenantComponentMap<T>
{
    private final MultiTenantCreator<T> creator;
    private final TenantReference tenantReference;
    private T component;

    public MockMultiTenantComponentMap(MultiTenantCreator<T> creator, TenantReference tenantReference)
    {
        this.creator = creator;
        this.tenantReference = tenantReference;
    }

    @Override
    public T get() throws IllegalStateException
    {
        if (component == null && creator != null)
        {
            component = creator.create(tenantReference.get());
        }
        return component;
    }

    @Override
    public Collection<T> getAll()
    {
        return Collections.singleton(get());
    }

    @Override
    public void initialiseAll()
    {
        get();
    }

    @Override
    public boolean isInitialised() throws IllegalStateException
    {
        return component != null;
    }

    @Override
    public void addInstance(T object) throws IllegalStateException
    {
        component = object;
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void onTenantStart(Tenant tenant)
    {
    }

    @Override
    public void onTenantStop(Tenant tenant)
    {
    }
}
