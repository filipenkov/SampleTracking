package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.api.model.AbstractResourceMethod;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.ArrayList;

/**
 * Default implementation of the {@link MethodInvocation}.  Continually calls interceptors until empty.
 *
 * @since 2.0 
 */
class DefaultMethodInvocation implements MethodInvocation
{
    private final List<ResourceInterceptor> interceptors;
    private final Object resource;
    private final HttpContext httpContext;
    private final AbstractResourceMethod method;
    private final Object[] parameters;

    public DefaultMethodInvocation(Object resource, AbstractResourceMethod method, HttpContext httpContext, List<ResourceInterceptor> interceptors,
                                   Object[] params)
    {
        this.resource = resource;
        this.method = method;
        this.httpContext = httpContext;
        this.interceptors = new ArrayList<ResourceInterceptor>(interceptors);
        this.parameters = params;
    }

    public Object getResource()
    {
        return resource;
    }

    public HttpContext getHttpContext()
    {
        return httpContext;
    }

    public AbstractResourceMethod getMethod()
    {
        return method;
    }

    public Object[] getParameters()
    {
        return parameters;
    }


    public void invoke() throws IllegalAccessException, InvocationTargetException
    {
        if (!interceptors.isEmpty())
        {
            ResourceInterceptor interceptor = interceptors.remove(0);
            interceptor.intercept(this);
        }
        else
        {
            throw new IllegalStateException("End of interceptor chain");
        }
    }
}
