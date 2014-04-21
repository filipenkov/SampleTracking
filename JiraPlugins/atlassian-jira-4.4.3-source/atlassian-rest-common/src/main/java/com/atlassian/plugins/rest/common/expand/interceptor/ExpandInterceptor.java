package com.atlassian.plugins.rest.common.expand.interceptor;

import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;
import com.atlassian.plugins.rest.common.expand.parameter.ExpandParameter;
import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.google.common.base.Preconditions;
import com.sun.jersey.api.core.HttpRequestContext;
import com.sun.jersey.api.core.HttpResponseContext;

import java.lang.reflect.InvocationTargetException;

/**
 * Expands the entity returned from the resource method
 *
 * @since 2.0
 */
public class ExpandInterceptor implements ResourceInterceptor
{
    private final EntityExpanderResolver expanderResolver;
    private final String expandParameterName;

    public ExpandInterceptor(EntityExpanderResolver expanderResolver)
    {
        this("expand", expanderResolver);
    }

    public ExpandInterceptor(String expandParameterName, EntityExpanderResolver expanderResolver)
    {
        this.expanderResolver = expanderResolver;
        this.expandParameterName = Preconditions.checkNotNull(expandParameterName);
    }
    
    public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        invocation.invoke();
        HttpRequestContext request = invocation.getHttpContext().getRequest();
        HttpResponseContext response = invocation.getHttpContext().getResponse();
        final ExpandParameter expandParameter = new DefaultExpandParameter(request.getQueryParameters().get(expandParameterName));
        new EntityCrawler().crawl(response.getEntity(), expandParameter, expanderResolver);
    }
}
