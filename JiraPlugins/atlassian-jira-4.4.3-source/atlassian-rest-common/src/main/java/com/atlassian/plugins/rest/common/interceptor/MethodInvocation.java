package com.atlassian.plugins.rest.common.interceptor;

import com.sun.jersey.api.model.AbstractResourceMethod;
import com.sun.jersey.api.core.HttpContext;

import java.lang.reflect.InvocationTargetException;

/**
 * Represents context information about a resource method invocation.
 *
 * @since 2.0
 */
public interface MethodInvocation
{
    /**
     * @return the resource object upon which the method resides
     */
    Object getResource();

    /**
     * @return the http context
     */
    HttpContext getHttpContext();

    /**
     * @return the method to be executed
     */
    AbstractResourceMethod getMethod();

    /**
     * Get the objects that will passed into the method.  The array is mutable.
     *
     * @return An array of objects
     */
    Object[] getParameters();

    /**
     * Called to invoke the next interceptor in the chain
     * @throws IllegalAccessException
     * @throws java.lang.reflect.InvocationTargetException
     */
    void invoke() throws IllegalAccessException, InvocationTargetException;
}
