package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.common.expand.interceptor.ExpandInterceptor;
import com.sun.jersey.spi.inject.SingletonTypeInjectableProvider;

import javax.ws.rs.core.Context;

/**
 * Constructs the {@link InterceptorChainBuilder}, registering it into Jersey
 *
 * @since 2.0
 */
public class InterceptorChainBuilderProvider extends SingletonTypeInjectableProvider<Context, InterceptorChainBuilder>
{
    public InterceptorChainBuilderProvider(AutowireCapablePlugin plugin, ExpandInterceptor expandInterceptor)
    {
        super(InterceptorChainBuilder.class, new InterceptorChainBuilder(plugin, expandInterceptor));
    }
}
