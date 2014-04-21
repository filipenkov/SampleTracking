package com.atlassian.plugins.rest.common.interceptor;

import java.lang.reflect.InvocationTargetException;

/**
 * Intercepts the execution of a resource method.  Implementations should call {@link MethodInvocation#invoke()} to
 * call the next interceptor in the chain.  Interceptor instances are created once for every resource method.
 *
 * @since 2.0
 */
public interface ResourceInterceptor
{
    /**
     * Intercepts the method invocation
     *
     * @param invocation Context information about the invocation
     */
    void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException;
}
