package com.atlassian.plugins.rest.common.interceptor.impl;

import com.atlassian.plugin.AutowireCapablePlugin;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import com.atlassian.plugins.rest.common.interceptor.impl.test.ClassResource;
import com.atlassian.plugins.rest.common.interceptor.impl.test.MethodResource;
import com.atlassian.plugins.rest.common.interceptor.impl.test.MyInterceptor;
import com.atlassian.plugins.rest.common.interceptor.impl.test.PackageResource;
import junit.framework.TestCase;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class InterceptorChainBuilderTest extends TestCase
{
    public void testMethodChain() throws NoSuchMethodException
    {
        verifyChain(MethodResource.class);
    }

    public void testClassChain() throws NoSuchMethodException
    {
        verifyChain(ClassResource.class);
    }

        public void testPackageChain() throws NoSuchMethodException
    {
        verifyChain(PackageResource.class);
    }

    public void testDefaultChain() throws NoSuchMethodException
    {
        AutowireCapablePlugin plugin = mock(AutowireCapablePlugin.class);
        InterceptorChainBuilder builder = new InterceptorChainBuilder(plugin);
        List<ResourceInterceptor> interceptors = builder.getResourceInterceptorsForMethod(Object.class.getMethod("hashCode"));
        assertNotNull(interceptors);
        assertEquals(0, interceptors.size());
    }

    private void verifyChain(Class resourceClass)
            throws NoSuchMethodException
    {
        AutowireCapablePlugin plugin = mock(AutowireCapablePlugin.class);
        when(plugin.autowire(MyInterceptor.class)).thenReturn(new MyInterceptor());
        InterceptorChainBuilder builder = new InterceptorChainBuilder(plugin);
        List<ResourceInterceptor> interceptors = builder.getResourceInterceptorsForMethod(resourceClass.getMethod("run"));
        assertNotNull(interceptors);
        assertEquals(1, interceptors.size());
        assertTrue("Interceptor was " + interceptors.get(0), interceptors.get(0) instanceof MyInterceptor);
    }


}
