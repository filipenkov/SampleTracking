package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import junit.framework.TestCase;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.mock;

public class DefaultMethodInvocationTest extends TestCase
{
    public void testInvoke() throws IllegalAccessException, InvocationTargetException
    {
        AtomicInteger counter = new AtomicInteger();
        CountingInterceptor interceptor1 = new CountingInterceptor(counter);
        CountingInterceptor interceptor2 = new CountingInterceptor(counter);
        DefaultMethodInvocation inv = new DefaultMethodInvocation(null, null, null, Arrays.<ResourceInterceptor>asList(
                interceptor1, interceptor2, mock(ResourceInterceptor.class)), new Object[0]);

        inv.invoke();
        assertEquals(0, interceptor1.before);
        assertEquals(1, interceptor2.before);
        assertEquals(2, interceptor2.after);
        assertEquals(3, interceptor1.after);
    }

    private static class CountingInterceptor implements ResourceInterceptor
    {
        private final AtomicInteger counter;
        public int before;
        public int after;

        public CountingInterceptor(AtomicInteger counter)
        {
            this.counter = counter;
        }

        public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
        {
            before = counter.getAndIncrement();
            invocation.invoke();
            after = counter.getAndIncrement();
        }
    }

}
