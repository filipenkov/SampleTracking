package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.MultiTenantLifecycleAware;
import com.atlassian.multitenant.MultiTenantManager;
import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.Callable;

/**
 */
public class MockMultiTenantManager implements MultiTenantManager
{
    private final TenantReference tenantReference;

    public MockMultiTenantManager(TenantReference tenantReference)
    {
        this.tenantReference = tenantReference;
    }

    @Override
    public void registerListener(MultiTenantLifecycleAware lifecycleListener)
    {
    }

    @Override
    public void deregisterListener(MultiTenantLifecycleAware lifecycleListener)
    {
    }

    @Override
    public void runForEachTenant(Runnable runnable, boolean override)
    {
        runnable.run();
    }

    @Override
    public void runForTenant(Tenant tenant, Runnable runnable, boolean override)
    {
        runnable.run();
    }

    @Override
    public <T> T callForTenant(Tenant tenant, Callable<T> callable, boolean override) throws Exception
    {
        return callable.call();
    }

    @Override
    public boolean isSingleTenantMode()
    {
        return true;
    }

    @Override
    public Collection<Tenant> getAllTenants()
    {
        return Collections.singleton(tenantReference.get());
    }

    @Override
    public Tenant getTenantFromSession(HttpSession session)
    {
        return tenantReference.get();
    }

    @Override
    public Tenant getTenantByName(String name)
    {
        return tenantReference.get();
    }

    @Override
    public boolean isSystemTenant()
    {
        return true;
    }
}
