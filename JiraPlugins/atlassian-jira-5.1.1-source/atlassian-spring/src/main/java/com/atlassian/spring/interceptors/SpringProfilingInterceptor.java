package com.atlassian.spring.interceptors;

import com.atlassian.util.profiling.ProfilingUtils;
import com.atlassian.util.profiling.UtilTimerStack;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 * A Spring AOP Interceptor to plug into the atlassian-profiling component.
 */
public class SpringProfilingInterceptor implements MethodInterceptor
{
    public Object invoke(MethodInvocation invocation) throws Throwable
    {
        if (!UtilTimerStack.isActive())
            return invocation.proceed();

        String name = ProfilingUtils.getJustClassName(invocation.getMethod().getDeclaringClass()) + "." + invocation.getMethod().getName() + "()";
        UtilTimerStack.push(name);
        try
        {
            return invocation.proceed();
        }
        finally
        {
            UtilTimerStack.pop(name);
        }
    }
}
