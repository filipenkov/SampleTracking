package com.atlassian.plugins.rest.module;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.sun.jersey.api.core.ResourceConfig;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.Set;

public class OsgiComponentProviderFactoryTest
{
    private OsgiComponentProviderFactory componentProviderFactory;

    private void initialiseComponentProviderFactory(Set<Class<?>> classes)
    {
        final ResourceConfig resourceConfig = mock(ResourceConfig.class);
        final OsgiPlugin plugin = mock(OsgiPlugin.class);

        when(resourceConfig.getClasses()).thenReturn(classes);

        componentProviderFactory = new OsgiComponentProviderFactory(resourceConfig, plugin);
    }

    @Test
    public void testGetComponentProviderWithNoResourceOrProviderClasses()
    {
        initialiseComponentProviderFactory(null);

        assertNull(componentProviderFactory.getComponentProvider(this.getClass()));
    }

    @Test
    public void testGetComponentProviderWithEmptyResourceOrProviderClasses()
    {
        initialiseComponentProviderFactory(Collections.<Class<?>>emptySet());

        assertNull(componentProviderFactory.getComponentProvider(this.getClass()));
    }

    @Test
    public void testGetComponentProviderWithAResourceOrProviderClasses()
    {
        initialiseComponentProviderFactory(Collections.<Class<?>>singleton(this.getClass()));

        assertNotNull(componentProviderFactory.getComponentProvider(this.getClass()));
        assertNull(componentProviderFactory.getComponentProvider(Object.class));
    }
}