package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.ExpandConstraint;
import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Testing {@link ExpandConstraintEntityExpanderResolver}
 */
public class ExpandConstraintEntityExpanderResolverTest
{
    private ExpandConstraintEntityExpanderResolver resolver;

    @Before
    public void setUp() throws Exception
    {
        resolver = new ExpandConstraintEntityExpanderResolver();
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
    public void testGetExpanderWithNullClass()
    {
        try
        {
            final Class clazz = null;
            resolver.getExpander(clazz);
            fail();
        }
        catch (NullPointerException e)
        {
            // expected
        }
    }

    @Test
    public void testHasExpanderWithClassForObjectWithNoConstraintMethod()
    {
        assertFalse(resolver.hasExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassForObjectWithNoConstraintMethod()
    {
        assertNull(resolver.getExpander(Object.class));
    }

    @Test
    public void testHasExpanderWithClassForClassWithConstraintMethod()
    {
        assertTrue(resolver.hasExpander(ClassWithConstraintMethod.class));
    }

    @Test
    public void testGetExpanderWithClassForClassWithConstraintMethod()
    {
        assertNotNull(resolver.getExpander(ClassWithConstraintMethod.class));
    }

    public static class ClassWithConstraintMethod
    {
        @ExpandConstraint
        public void expand(Indexes indexes)
        {
        }
    }
}
