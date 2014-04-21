package com.atlassian.jira.mock.multitenant;

import com.atlassian.multitenant.MultiTenantComponentFactory;
import com.atlassian.multitenant.MultiTenantComponentMap;
import com.atlassian.multitenant.MultiTenantComponentMapBuilder;
import com.atlassian.multitenant.MultiTenantCreator;
import com.atlassian.multitenant.TenantReference;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Set;

/**
 * Factory that doesn't create proxies, rather, always returns a direct reference to the instance provided by the
 * creator or map
 */
public class MockMultiTenantComponentFactory implements MultiTenantComponentFactory
{
    private final TenantReference tenantReference;

    public MockMultiTenantComponentFactory(TenantReference tenantReference)
    {
        this.tenantReference = tenantReference;
    }

    @Override
    public <C> MultiTenantComponentMapBuilder<C> createComponentMapBuilder(MultiTenantCreator<C> creator)
    {
        return new MockMultiTenantComponentMapBuilder<C>(createComponentMap(creator));
    }

    @Override
    public <C> MultiTenantComponentMap<C> createComponentMap(MultiTenantCreator<C> creator)
    {
        return new MockMultiTenantComponentMap<C>(creator, tenantReference);
    }

    @Override
    public <C> Object createComponent(final MultiTenantComponentMap<C> map, ClassLoader classLoader, Class<? super C>... interfaces)
    {
        return Proxy.newProxyInstance(classLoader, interfaces, new InvocationHandler()
        {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
            {
                try
                {
                    return method.invoke(map.get(), args);
                }
                catch (InvocationTargetException ite)
                {
                    throw ite.getCause();
                }
            }
        });
    }

    @Override
    public <C> C createComponent(MultiTenantComponentMap<C> map, Class<C> inter)
    {
        return (C) createComponent(map, inter.getClassLoader(), inter);
    }

    @Override
    public <C> C createComponent(MultiTenantCreator<C> creator, Class<C> inter)
    {
        return createComponent(createComponentMap(creator), inter);
    }

    @Override
    public <C> C createComponent(Class<? extends C> clazz, Class<C> inter)
    {
        try
        {
            return clazz.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <C> C createComponent(MultiTenantComponentMap<C> map, Set<Method> invokeForAllMethods, Class<C> inter)
    {
        return createComponent(map, inter);
    }

    @Override
    public <C> Object createComponent(MultiTenantComponentMap<C> map, ClassLoader classLoader, Set<Method> invokeForAllMethods, Class<? super C>... interfaces)
    {
        return createComponent(map, classLoader, interfaces);
    }

    @Override
    public <C> C createEnhancedComponent(MultiTenantComponentMap<C> map, Class superClass)
    {
        return map.get();
    }

    @Override
    public <C> C createEnhancedComponent(MultiTenantCreator<C> creator, Class<C> superClass)
    {
        return creator.create(tenantReference.get());
    }

    @Override
    public <C> C createEnhancedComponent(Class<C> superClass)
    {
        try
        {
            return superClass.newInstance();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
