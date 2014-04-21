package com.atlassian.jira;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.DelegateComponentAdapter.Builder;
import org.picocontainer.ComponentAdapter;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;
import org.picocontainer.PicoInitializationException;
import org.picocontainer.PicoIntrospectionException;
import org.picocontainer.PicoVerificationException;
import org.picocontainer.defaults.AbstractComponentAdapter;
import org.picocontainer.defaults.DefaultPicoContainer;

import java.util.concurrent.atomic.AtomicBoolean;

public class TestDelegateComponentAdapter extends ListeningTestCase
{
    interface One
    {
        int one();
    }

    interface Two
    {
        int two();
    }

    public static class OneTwoImpl implements One, Two
    {
        int one, two;

        public int one()
        {
            return ++one;
        }

        public int two()
        {
            return ++two;
        }
    }

    @Test
    public void testAdapter() throws Exception
    {
        final MutablePicoContainer container = new DefaultPicoContainer();

        container.registerComponentImplementation(One.class, OneTwoImpl.class);
        assertNotNull(container.getComponentInstance(One.class));
        final One one = (One) container.getComponentInstance(One.class);
        assertNotNull(one);
        assertNull(container.getComponentInstance(Two.class));
        container.registerComponent(new DelegateComponentAdapter<Two>(Two.class, container.getComponentAdapter(One.class)));
        final Two two = (Two) container.getComponentInstance(Two.class);
        assertNotNull(two);
        assertSame(one, two);
        assertSame(one, two);
    }

    @Test
    public void testBuilderRegistersOne() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();

        final DelegateComponentAdapter.Builder<OneTwoImpl> builder = DelegateComponentAdapter.Builder.builderFor(OneTwoImpl.class);
        builder.implementing(One.class);
        builder.registerWith(ComponentContainer.Scope.INTERNAL, container);
        assertNotNull(container.getComponentInstance(One.class));
    }

    @Test
    public void testBuilderRegistersOneAndTwo() throws Exception
    {
        final ComponentContainer container = new ComponentContainer();

        Builder.builderFor(OneTwoImpl.class).implementing(One.class).implementing(Two.class).registerWith(ComponentContainer.Scope.INTERNAL,
            container);
        final One one = container.getComponentInstance(One.class);
        assertNotNull(one);
        final Two two = container.getComponentInstance(Two.class);
        assertNotNull(two);
        assertSame(one, two);
    }

    @Test
    public void testBuilderWithNullClass() throws Exception
    {
        try
        {
            Builder.builderFor(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testBuilderWithNoInterfaces() throws Exception
    {
        try
        {
            Builder.builderFor(OneTwoImpl.class).registerWith(ComponentContainer.Scope.INTERNAL, new ComponentContainer());
            fail("IllegalStateException expected");
        }
        catch (final IllegalStateException expected)
        {}
    }

    @Test
    public void testAdapterWithNullImplementer() throws Exception
    {
        try
        {
            new DelegateComponentAdapter<OneTwoImpl>(null, new AbstractComponentAdapter(new Object(), String.class)
            {
                public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
                {
                    return null;
                }

                public void verify() throws PicoVerificationException
                {}
            });
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testAdapterWithNullDelegate() throws Exception
    {
        try
        {
            new DelegateComponentAdapter<OneTwoImpl>(One.class, null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testAdapterDelegatesVerify() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final ComponentAdapter adapter = new DelegateComponentAdapter<OneTwoImpl>(One.class, new AbstractComponentAdapter(new Object(), String.class)
        {
            public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
            {
                return null;
            }

            public void verify() throws PicoVerificationException
            {
                called.set(true);
            }
        });
        assertFalse(called.get());
        adapter.verify();
        assertTrue(called.get());
    }

    @Test
    public void testAdapterDelegatesGetComponentInstance() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final ComponentAdapter adapter = new DelegateComponentAdapter<OneTwoImpl>(One.class, new AbstractComponentAdapter(new Object(), String.class)
        {
            public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
            {
                called.set(true);
                return null;
            }

            public void verify() throws PicoVerificationException
            {}
        });
        assertFalse(called.get());
        assertNull(adapter.getComponentInstance());
        assertTrue(called.get());
    }

    @Test
    public void testAdapterDelegatesGetContainer() throws Exception
    {
        final AtomicBoolean called = new AtomicBoolean();
        final ComponentAdapter adapter = new DelegateComponentAdapter<OneTwoImpl>(One.class, new AbstractComponentAdapter(new Object(), String.class)
        {
            @Override
            public PicoContainer getContainer()
            {
                called.set(true);
                return super.getContainer();
            }

            public Object getComponentInstance() throws PicoInitializationException, PicoIntrospectionException
            {
                return null;
            }

            public void verify() throws PicoVerificationException
            {}
        });
        assertFalse(called.get());
        assertNull(adapter.getContainer());
        assertTrue(called.get());
    }
}
