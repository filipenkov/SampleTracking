package com.atlassian.plugins.rest.module.expand.resolver;

import com.atlassian.plugin.osgi.factory.OsgiPlugin;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.Expander;
import com.atlassian.plugins.rest.common.expand.resolver.EntityExpanderResolver;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Testing {@link PluginEntityExpanderResolver}
 */
public class OsgiPluginEntityExpanderResolverTest
{
    private PluginEntityExpanderResolver resolver;
    private EntityExpander entityExpander;

    @Before
    public void setUp() throws Exception
    {
        entityExpander = mock(AnnotatedClassEntityExpander.class);
        OsgiPlugin osgiPlugin = mock(OsgiPlugin.class);
        when(osgiPlugin.autowire(AnnotatedClassEntityExpander.class)).thenReturn((AnnotatedClassEntityExpander) entityExpander);

        resolver = new PluginEntityExpanderResolver(osgiPlugin);
    }

    @Test
    public void testConstructorWithNullPlugin()
    {
        try
        {
            new PluginEntityExpanderResolver(null);
            fail();
        }
        catch (NullPointerException e)
        {
            // expected
        }
    }

    @Test
    public void testHasExpanderWithNullClass()
    {
        try
        {
            final Class clazz = null;
            resolver.hasExpander(clazz);
            fail();
        }
        catch (NullPointerException e)
        {
            // expected
        }
    }

    @Test
    public void testHasExpanderWithAnnotatedClass()
    {
        assertTrue(resolver.hasExpander(AnnotatedClass.class));
    }

    @Test
    public void testHasExpanderWithNonAnnotatedClass()
    {
        assertFalse(resolver.hasExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithAnnotatedClass()
    {
        assertEquals(entityExpander, resolver.getExpander(AnnotatedClass.class));
    }

    @Test
    public void testGetExpanderWithNonAnnotatedClass()
    {
        assertNull(resolver.getExpander(Object.class));
    }

    @Expander(AnnotatedClassEntityExpander.class)
    private static class AnnotatedClass
    {
    }

    private static class AnnotatedClassEntityExpander implements EntityExpander<AnnotatedClass>
    {
        public AnnotatedClass expand(ExpandContext expandContext, EntityExpanderResolver expanderResolver, EntityCrawler entityCrawler)
        {
            return null;
        }
    }
}
