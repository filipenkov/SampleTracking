package com.atlassian.plugins.rest.common.interceptor.impl.test;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;

import java.lang.reflect.InvocationTargetException;

public class MyInterceptor implements ResourceInterceptor
{
    private boolean called;
    public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        called = true;
        invocation.invoke();
    }

    public boolean isCalled()
    {
        return called;
    }
}
