package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.common.interceptor.InterceptorChain;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Builds the interceptor chain for the resource method.  Uses the {@link InterceptorChain} resource by building the
 * chain from the method then class then package, then default interceptors passed into the constructor.
 *
 * This is a private class and used by the  {@link EntityParamDispatchProviderWrapper}
 * and {@link com.atlassian.plugins.rest.common.multipart.jersey.MultipartFormDispatchProvider}
 * which both will use this helper class to wrap calls to rest methods with interceptors.
 *
 * @since 2.0
 */
public class InterceptorChainBuilder
{
    private final LinkedHashMap<Class<? extends ResourceInterceptor>, ResourceInterceptor> defaultResourceInterceptors;
    private final AutowireCapablePlugin plugin;

    public InterceptorChainBuilder(AutowireCapablePlugin plugin, ResourceInterceptor... resourceInterceptors)
    {
        defaultResourceInterceptors = new LinkedHashMap<Class<? extends ResourceInterceptor>, ResourceInterceptor>();
        for (ResourceInterceptor resourceInterceptor : resourceInterceptors)
        {
            defaultResourceInterceptors.put(resourceInterceptor.getClass(), resourceInterceptor);
        }
        this.plugin = plugin;
    }

    public List<ResourceInterceptor> getResourceInterceptorsForMethod(Method m)
    {
        // First check the method
        InterceptorChain chain = m.getAnnotation(InterceptorChain.class);
        if (chain == null)
        {
            // Next check the class
            chain = m.getDeclaringClass().getAnnotation(InterceptorChain.class);
            if (chain == null)
            {
                // Finally, check the package
                chain = m.getDeclaringClass().getPackage().getAnnotation(InterceptorChain.class);
            }
        }

        if (chain != null)
        {
            return buildFromClass(chain.value());
        }
        else
        {
            // Return default interceptor list
            return new ArrayList<ResourceInterceptor>(defaultResourceInterceptors.values());
        }
    }

    private List<ResourceInterceptor> buildFromClass(Class<? extends ResourceInterceptor>[] resourceInterceptorClasses)
    {
        List<ResourceInterceptor> resourceInterceptors = new ArrayList<ResourceInterceptor>();
        for (Class<? extends ResourceInterceptor> resourceInterceptorClass : resourceInterceptorClasses)
        {
            if (defaultResourceInterceptors.containsKey(resourceInterceptorClass))
            {
                resourceInterceptors.add(defaultResourceInterceptors.get(resourceInterceptorClass));
            }
            else
            {
                resourceInterceptors.add(plugin.autowire(resourceInterceptorClass));

                // todo: we should find a way to autowire from jersey injectables
            }
        }
        return resourceInterceptors;
    }
}
