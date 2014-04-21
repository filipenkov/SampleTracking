package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.EntityExpander;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.Expander;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.Mockito.mock;

/**
 * Testing {@link AbstractAnnotationEntityExpanderResolver}
 */
public class AbstractAnnotationEntityExpanderResolverTest
{
    private AbstractAnnotationEntityExpanderResolver resolver;
    private EntityExpander entityExpander;

    @Before
    public void setUp() throws Exception
    {
        entityExpander = mock(EntityExpander.class);

        resolver = new AbstractAnnotationEntityExpanderResolver()
        {
            protected EntityExpander<?> getEntityExpander(Expander e)
            {
                if (e.value().equals(AnnotatedClassEntityExpander.class))
                {
                    return entityExpander;
                }
                fail("Should not have been called with a different expander");
                return null; // just for compilation
            }
        };
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
