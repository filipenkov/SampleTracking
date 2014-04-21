package com.atlassian.applinks.core.rest.context;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;

import java.lang.reflect.InvocationTargetException;

/**
 * Interceptor to allow capturing the REST context for the current thread. Means context params do not need to be passed
 * through resource methods.
 */
public class ContextInterceptor implements ResourceInterceptor
{
    public void intercept(final MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            CurrentContext.setContext(invocation.getHttpContext());
            invocation.invoke();
        }
        finally
        {
            CurrentContext.setContext(null);
        }
    }
}