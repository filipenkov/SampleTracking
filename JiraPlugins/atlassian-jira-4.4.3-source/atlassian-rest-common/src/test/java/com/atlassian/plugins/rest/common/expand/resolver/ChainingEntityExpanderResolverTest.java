package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.google.common.collect.Lists;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.util.Collections;

public class ChainingEntityExpanderResolverTest
{
    private ChainingEntityExpanderResolver resolver;
    private EntityExpanderResolver resolverItem1;
    private EntityExpanderResolver resolverItem2;

    @Before
    public void setUp() throws Exception
    {
        resolverItem1 = mock(EntityExpanderResolver.class);
        resolverItem2 = mock(EntityExpanderResolver.class);

        resolver = new ChainingEntityExpanderResolver(Lists.newArrayList(resolverItem1, resolverItem2));
    }

    @Test
    public void testConstructorWithNullList()
    {
        try
        {
            new ChainingEntityExpanderResolver(null);
            fail();
        }
        catch (NullPointerException e)
        {
            // expected
        }
    }

    @Test
    public void testConstructorWithListWithNullItem()
    {
        try
        {
            new ChainingEntityExpanderResolver(Collections.<EntityExpanderResolver>singletonList(null));
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
    public void testHasExpanderWithClassAndNoResolverHasExpander()
    {
        when(resolverItem1.hasExpander(Object.class)).thenReturn(false);
        when(resolverItem2.hasExpander(Object.class)).thenReturn(false);

        assertFalse(resolver.hasExpander(Object.class));
    }

    @Test
    public void testHasExpanderWithClassAndResolver2HasExpander()
    {
        when(resolverItem1.hasExpander(Object.class)).thenReturn(false);
        when(resolverItem2.hasExpander(Object.class)).thenReturn(true);

        assertTrue(resolver.hasExpander(Object.class));
    }

    @Test
    public void testHasExpanderWithClassAndResolver1HasExpander()
    {
        when(resolverItem1.hasExpander(Object.class)).thenReturn(true);
        verify(resolverItem2, never()).hasExpander(Object.class);

        assertTrue(resolver.hasExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassAndNoResolverHasExpander()
    {
        when(resolverItem1.getExpander(Object.class)).thenReturn(null);
        when(resolverItem2.getExpander(Object.class)).thenReturn(null);

        assertNull(resolver.getExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassAndResolver2HasExpander()
    {
        final EntityExpander<Object> entityExpander = mock(EntityExpander.class);
        when(resolverItem1.getExpander(Object.class)).thenReturn(null);
        when(resolverItem2.getExpander(Object.class)).thenReturn(entityExpander);

        assertEquals(entityExpander, resolver.getExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassAndResolver1HasExpander()
    {
        final EntityExpander<Object> entityExpander = mock(EntityExpander.class);
        when(resolverItem1.getExpander(Object.class)).thenReturn(entityExpander);
        verify(resolverItem2, never()).getExpander(Object.class);

        assertEquals(entityExpander, resolver.getExpander(Object.class));
    }
}
