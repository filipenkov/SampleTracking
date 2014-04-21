package com.atlassian.event.internal;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import java.util.List;

public abstract class AbstractEventPublisherAnnotatedListenerMethodTest
{
    private EventPublisher eventPublisher;

    @Before
    public final void setUp()
    {
        eventPublisher = getEventPublisherForTest();
    }

    abstract EventPublisher getEventPublisherForTest();

    @After
    public final void tearDown() throws Exception
    {
        eventPublisher = null;
    }

    @Test(expected = NullPointerException.class)
    public final void testPublishNullEvent()
    {
        eventPublisher.publish(null);
    }

    @Test
    public final void testRegisterListener()
    {
        final Object event = new _TestEvent(this);
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size());
        assertSame(event, listener.testEvents.get(0));
    }

    @Test
    public final void testListenerWithoutMatchingEventClass()
    {
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(new Object());

        assertTrue(listener.testEvents.isEmpty());
    }

    @Test
    public final void testUnRegisterListener()
    {
        final Object event = new _TestEvent(this);
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size());
        assertSame(event, listener.testEvents.get(0));

        eventPublisher.unregister(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size()); // not called anymore
    }

    @Test
    public final void testUnRegisterAll()
    {
        final Object event = new _TestEvent(this);
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size());
        assertSame(event, listener.testEvents.get(0));

        eventPublisher.unregisterAll();
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size()); // not called anymore
    }

    @Test
    public final void testInterfacesHandled() throws Exception
    {
        final Object event = new AnInterface()
        {};
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testInterfaceEvents.size());
        assertSame(event, listener.testInterfaceEvents.get(0));
    }

    @Test
    public final void testChildrenHandled() throws Exception
    {
        final Object event = new SubTestEvent(this);
        final TestListener listener = new TestListener();

        eventPublisher.register(listener);
        eventPublisher.publish(event);

        assertEquals(1, listener.testEvents.size());
        assertSame(event, listener.testEvents.get(0));
    }

    @Test
    public final void testAllHandlersInvokedWhenExceptionEncountered() throws Exception
    {
        final Object event = new SubTestEvent(this);
        final TestListener listenerBefore = new TestListener();
        final TestListenerThatThrowsException listenerThatThrowsException = new TestListenerThatThrowsException();
        final TestListener listenerAfter = new TestListener();

        eventPublisher.register(listenerBefore);
        eventPublisher.register(listenerThatThrowsException);
        eventPublisher.register(listenerAfter);
        eventPublisher.publish(event);

        assertEquals(1, listenerBefore.testEvents.size());
        assertEquals(1, listenerThatThrowsException.testEvents.size());
        assertEquals(1, listenerAfter.testEvents.size());
    }

    private static class TestListener
    {
        protected final List<Object> testEvents = Lists.newArrayList();
        protected final List<Object> testInterfaceEvents = Lists.newArrayList();

        @SuppressWarnings("unused")
        @EventListener
        public void onEvent(final _TestEvent event)
        {
            testEvents.add(event);
        }

        @SuppressWarnings("unused")
        @EventListener
        public void onInterface(final AnInterface event)
        {
            testInterfaceEvents.add(event);
        }
    }

    private static class TestListenerThatThrowsException extends TestListener
    {
        @SuppressWarnings("unused")
        @EventListener
        @Override
        public void onEvent(final _TestEvent event)
        {
            super.onEvent(event);
            throw new RuntimeException("event exception");
        }

        @SuppressWarnings("unused")
        @EventListener
        @Override
        public void onInterface(final AnInterface event)
        {
            super.onInterface(event);
            throw new RuntimeException("event interface exception");
        }
    }

    private static interface AnInterface
    {}

    private static class SubTestEvent extends _TestEvent
    {
        public SubTestEvent(final Object src)
        {
            super(src);
        }
    }
}
