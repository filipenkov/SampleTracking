package com.atlassian.plugins.rest.common.interceptor.impl;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.server.impl.model.method.dispatch.EntityParamDispatchProvider;
import com.sun.jersey.spi.dispatch.RequestDispatcher;

import javax.ws.rs.core.Context;

/**
 * Wraps the {@link EntityParamDispatchProvider} while hooking in the interceptor chain execution
 *
 * @since 2.0
 */
public class EntityParamDispatchProviderWrapper extends EntityParamDispatchProvider
{
    private @Context InterceptorChainBuilder interceptorChainBuilder;

    @Override
    public RequestDispatcher create(AbstractResourceMethod abstractResourceMethod)
    {
        DispatchProviderHelper helper = new DispatchProviderHelper(interceptorChainBuilder);
        return helper.create(abstractResourceMethod, getInjectableValuesProvider(abstractResourceMethod));
    }
}