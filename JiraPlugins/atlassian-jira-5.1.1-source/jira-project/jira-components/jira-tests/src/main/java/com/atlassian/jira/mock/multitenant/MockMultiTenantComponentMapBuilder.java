package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantComponentMapBuilder;
import com.atlassian.multitenant.MultiTenantDestroyer;

/**
 * Builder that doesn't build anything but just returns the map it was constructed with.
 */
public class MockMultiTenantComponentMapBuilder<T> implements MultiTenantComponentMapBuilder<T>
{
    private final MultiTenantComponentMap<T> map;

    public MockMultiTenantComponentMapBuilder(MultiTenantComponentMap<T> map)
    {
        this.map = map;
    }

    @Override
    public MultiTenantComponentMapBuilder<T> setDestroyer(MultiTenantDestroyer multiTenantDestroyer)
    {
        return this;
    }

    @Override
    public MultiTenantComponentMapBuilder<T> setLazyLoad(MultiTenantComponentMap.LazyLoadStrategy lazyLoadStrategy)
    {
        return this;
    }

    @Override
    public MultiTenantComponentMapBuilder<T> setNoTenantStrategy(MultiTenantComponentMap.NoTenantStrategy strategy)
    {
        return this;
    }

    @Override
    public MultiTenantComponentMapBuilder<T> registerListener(MultiTenantComponentMap.Registration choice)
    {
        return this;
    }

    @Override
    public MultiTenantComponentMap<T> construct()
    {
        return map;
    }
}
