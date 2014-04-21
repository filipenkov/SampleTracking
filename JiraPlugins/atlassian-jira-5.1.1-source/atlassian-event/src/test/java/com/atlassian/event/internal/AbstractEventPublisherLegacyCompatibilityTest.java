package com.atlassian.event.internal;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.atlassian.event.Event;
import com.atlassian.event.EventListener;
import com.atlassian.event.EventManager;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.event.legacy.LegacyEventManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * <strong>Note:</strong> This is more of a (small) integration test as we use a concrete {@link com.atlassian.event.legacy.LegacyListenerHandler}
 * at runtime here (and not a stub/mock). It essentially ensure that we get the same behavior at the legacy event manager.
 */
@SuppressWarnings("deprecation")
public abstract class AbstractEventPublisherLegacyCompatibilityTest
{
    private EventManager eventManager;
    private EventPublisher eventPublisher;

    @Before
    public final void setUp()
    {
        eventPublisher = getEventPublisher();
        eventManager = new LegacyEventManager(eventPublisher);
    }

    abstract EventPublisher getEventPublisher();

    @After
    public final void tearDown() throws Exception
    {
        eventManager = null;
        eventPublisher = null;
    }

    @Test(expected = NullPointerException.class)
    public final void testPublishNullEvent()
    {
        eventManager.publishEvent(null);
    }

    @Test
    public final void testRegisterListener()
    {
        final Event event = new _TestEvent(this);
        final EventListener listener = getListener(_TestEvent.class);

        eventManager.registerListener("key", listener);

        verifyEventReceived(event, listener);
    }

    @Test
    public final void testListenerWithoutMatchingEventClass()
    {
        final Event event = getEvent();
        final EventListener listener = getListener(_TestEvent.class);

        eventManager.registerListener("key1", listener);
        eventManager.publishEvent(event);

        verify(listener, never()).handleEvent(event);
    }

    @Test
    public final void testUnregisterListener()
    {
        final Event event = new _TestEvent(this);
        final EventListener listener = getListener(_TestEvent.class);

        eventManager.registerListener("key", listener);
        eventManager.publishEvent(event);
        eventPublisher.publish(event);

        verify(listener, times(2)).handleEvent(event);

        eventManager.unregisterListener("key");
        eventManager.publishEvent(event);
        eventPublisher.publish(event);

        verify(listener, times(2)).handleEvent(event); // i.e it's no called again
    }

    @Test
    public final void testListensForEverything()
    {
        final Event event = new _TestEvent(this);
        final EventListener listener = getListener();

        eventManager.registerListener("key", listener);
        verifyEventReceived(event, listener);
    }

    @Test
    public final void testListensForEverythingDoesNotReceiveNonEventClasses()
    {
        final EventListener listener = getListener();

        eventManager.registerListener("key", listener);
        eventPublisher.publish("Cheese");

        verify(listener, never()).handleEvent(any(Event.class));
    }

    @Test
    public final void testRemoveNonExistentListener()
    {
        final EventListener listener = getListener(Event.class);

        eventManager.registerListener("key", listener);
        eventManager.unregisterListener("this.key.is.not.registered");
    }

    @Test
    public final void testDuplicateKeysForListeners()
    {
        final String key = "key1";
        final Event event = new _TestEvent(this);
        final EventListener listener1 = getListener(_TestEvent.class);
        final EventListener listener2 = getListener(_TestEvent.class);

        eventManager.registerListener(key, listener1);
        eventManager.registerListener(key, listener2);

        eventManager.publishEvent(event);
        eventPublisher.publish(event);

        verify(listener1, never()).handleEvent(event);
        verify(listener2, times(2)).handleEvent(event);

        eventManager.unregisterListener(key);
        eventManager.publishEvent(event);
        eventPublisher.publish(event);

        verify(listener1, never()).handleEvent(event);
        verify(listener2, times(2)).handleEvent(event); // i.e. it's not been called again
    }

    @Test(expected = NullPointerException.class)
    public final void testAddValidKeyWithNullListener()
    {
        eventManager.registerListener("key1", null);
    }

    @Test
    public final void testInterfacesHandled() throws Exception
    {
        final Event event = new TestInterfacedEvent(this);
        final EventListener listener = getListener(TestInterface.class);

        eventManager.registerListener("key1", listener);
        verifyEventReceived(event, listener);
    }

    @Test
    public final void testChildrenHandled() throws Exception
    {
        final Event event = new TestSubEvent(this);
        final EventListener listener = getListener(_TestEvent.class);

        eventManager.registerListener("key1", listener);
        verifyEventReceived(event, listener);
    }

    @Test
    public final void testGrandChildrenHandled() throws Exception
    {
        final Event event = new TestSubSubEvent(this);
        final EventListener listener = getListener(_TestEvent.class);

        eventManager.registerListener("key1", listener);
        verifyEventReceived(event, listener);
    }

    @Test
    public final void testOneEventForTwoHandledClasses()
    {
        final Event event = new TestSubSubEvent(this);
        final EventListener listener = getListener(_TestEvent.class, TestSubEvent.class);

        eventManager.registerListener("key1", listener);
        eventManager.publishEvent(event);

        verify(listener, times(1)).handleEvent(event);
    }

    private void verifyEventReceived(final Event event, final EventListener listener)
    {
        eventManager.publishEvent(event);
        eventPublisher.publish(event);
        verify(listener, times(2)).handleEvent(event);
    }

    protected Event getEvent()
    {
        return new Event(this)
        {};
    }

    protected EventListener getListener(final Class<?>... classes)
    {
        final EventListener listener = mock(EventListener.class);
        when(listener.getHandledEventClasses()).thenReturn(classes);
        return listener;
    }

    private static class TestSubEvent extends _TestEvent
    {
        public TestSubEvent(final Object src)
        {
            super(src);
        }
    }

    private static class TestSubSubEvent extends TestSubEvent
    {
        public TestSubSubEvent(final Object src)
        {
            super(src);
        }
    }

    private static class TestInterfacedEvent extends Event implements TestInterface
    {
        public TestInterfacedEvent(final Object src)
        {
            super(src);
        }
    }

    private static interface TestInterface
    {}
}
