package com.atlassian.plugins.rest.common.expand.resolver;

import com.atlassian.plugins.rest.common.expand.DefaultExpandContext;
import com.atlassian.plugins.rest.common.expand.EntityCrawler;
import com.atlassian.plugins.rest.common.expand.ExpandContext;
import com.atlassian.plugins.rest.common.expand.Expandable;
import com.atlassian.plugins.rest.common.expand.entity.AbstractPagedListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapper;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallBacks;
import com.atlassian.plugins.rest.common.expand.entity.ListWrapperCallback;
import com.atlassian.plugins.rest.common.expand.parameter.DefaultExpandParameter;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singleton;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Testing {@link ListWrapperEntityExpanderResolver}
 */
public class ListWrapperEntityExpanderResolverTest
{
    private ListWrapperEntityExpanderResolver resolver;

    @Before
    public void setUp() throws Exception
    {
        resolver = new ListWrapperEntityExpanderResolver();
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
    public void testHasExpanderWithClassForNonListWrapper()
    {
        assertFalse(resolver.hasExpander(Object.class));
    }

    @Test
    public void testGetExpanderWithClassForNonListWrapper()
    {
        assertNull(resolver.getExpander(Object.class));
    }

    @Test
    public void testHasExpanderWithClassForListWrapper()
    {
        assertTrue(resolver.hasExpander(ListWrapper.class));
    }

    @Test
    public void testGetExpanderWithClassForListWrapper()
    {
        assertEquals(ListWrapperEntityExpanderResolver.EXPANDER, resolver.getExpander(ListWrapper.class));
    }

    @Test
    @SuppressWarnings ("unchecked")
    public void testExpandCollection() throws Exception
    {
        final List<String> strings = Arrays.asList("one", "two");
        final ClassWithExpandableList<String> listWrapper = new ClassWithExpandableList<String>(strings);

        Expandable expandable = expandable("list");

        EntityCrawler crawler = Mockito.mock(EntityCrawler.class);
        ExpandContext<ListWrapper<String>> context = new DefaultExpandContext<ListWrapper<String>>(listWrapper, expandable, new DefaultExpandParameter(singleton("list.whatever")));

        // run the test
        new ListWrapperEntityExpanderResolver.ListWrapperEntityExpander().expand(context, resolver, crawler);

        // check that the list was expanded
        assertTrue(listWrapper.items != null);
        assertEquals(listWrapper.items.size(), strings.size());
    }

    @Test
    @SuppressWarnings ("unchecked")
    public void testExpandInheritedCollection() throws Exception
    {
        final List<String> strings = Arrays.asList("one", "two");
        final ClassWithExpandableList<String> listWrapper = new ClassWithInheritedExpandableList<String>(strings);

        Expandable expandable = expandable("list");

        EntityCrawler crawler = Mockito.mock(EntityCrawler.class);
        ExpandContext<ListWrapper<String>> context = new DefaultExpandContext<ListWrapper<String>>(listWrapper, expandable, new DefaultExpandParameter(singleton("list.whatever")));

        // run the test
        new ListWrapperEntityExpanderResolver.ListWrapperEntityExpander().expand(context, resolver, crawler);

        // check that the list was expanded
        assertTrue(listWrapper.items != null);
        assertEquals(listWrapper.items.size(), strings.size());
    }

    static class ClassWithExpandableList<T> extends AbstractPagedListWrapper<T>
    {
        List<T> items = null;
        final ListWrapperCallback<T> cb;

        ClassWithExpandableList(List<T> wrappedList)
        {
            super(wrappedList.size(), Integer.MAX_VALUE);
            cb = ListWrapperCallBacks.ofList(wrappedList);
        }

        @Override
        public ListWrapperCallback<T> getPagingCallback()
        {
            return cb;
        }
    }

    static class ClassWithInheritedExpandableList<T> extends ClassWithExpandableList<T>
    {

        ClassWithInheritedExpandableList(List<T> wrappedList)
        {
            super(wrappedList);
        }
    }

    static Expandable expandable(final String value)
    {
        return new Expandable()
        {
            public String value()
            {
                return value;
            }

            public Class<? extends Annotation> annotationType()
            {
                return Expandable.class;
            }
        };
    }
}
