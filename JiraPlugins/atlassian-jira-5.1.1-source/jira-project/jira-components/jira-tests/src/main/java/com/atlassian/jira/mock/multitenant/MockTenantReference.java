package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.Tenant;
import com.atlassian.multitenant.TenantReference;

/**
 * Mock tenant reference.  This tenant reference always returns the same tenant.
 */
public class MockTenantReference implements TenantReference
{
    private final Tenant tenant;

    public MockTenantReference(Tenant tenant)
    {
        this.tenant = tenant;
    }

    @Override
    public Tenant get() throws IllegalStateException
    {
        return tenant;
    }

    @Override
    public boolean isSet()
    {
        return true;
    }

    @Override
    public void set(Tenant tenant, boolean allowOverride) throws IllegalStateException
    {
    }

    @Override
    public void remove() throws IllegalStateException
    {
    }
}
