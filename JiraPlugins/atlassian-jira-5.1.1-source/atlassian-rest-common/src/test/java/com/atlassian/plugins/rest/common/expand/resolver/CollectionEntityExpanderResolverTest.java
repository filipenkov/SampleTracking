package com.atlassian.plugins.rest.common.expand.resolver;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Testing {@link CollectionEntityExpanderResolver}
 */
public class CollectionEntityExpanderResolverTest
{
    private CollectionEntityExpanderResolver resolver;

    @Before
    public void setUp() throws Exception
    {
        resolver = new CollectionEntityExpanderResolver();
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
    public void testHasExpanderWithClassForCollection()
    {
        assertTrue(resolver.hasExpander(Collection.class));
    }

    @Test
    public void testHasExpanderWithClassForList()
    {
        assertTrue(resolver.hasExpander(List.class));
    }

    @Test
    public void testHasExpanderWithClassForObject()
    {
        assertFalse(resolver.hasExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassForCollection()
    {
        assertNotNull(resolver.getExpander(Collection.class));
    }

    @Test
    public void testGetExpanderWithClassForList()
    {
        assertNotNull(resolver.getExpander(List.class));
    }

    @Test
    public void testGetExpanderWithClassForObject()
    {
        assertNull(resolver.getExpander(Object.class));
    }
}
