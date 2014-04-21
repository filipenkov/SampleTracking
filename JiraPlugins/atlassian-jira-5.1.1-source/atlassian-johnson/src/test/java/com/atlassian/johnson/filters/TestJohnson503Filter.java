package com.atlassian.johnson.filters;

import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import junit.framework.TestCase;
import org.easymock.MockControl;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class TestJohnson503Filter extends TestCase
{

    private static final EventType DEFAULT_EVENT_TYPE = new EventType("event-type", "event-type-desc");

    public void testHandleErrorWithNullContainer() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(null, null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    public void testHandleErrorWithEmptyContainer() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(createContainer(), null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    public void testHandleErrorWithWarningOnly() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.setHeader("Retry-After", "30");
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(createContainer(EventLevel.WARNING), null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    public void testHandleErrorWithErrorOnly() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(createContainer(EventLevel.ERROR), null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }
    public void testHandleErrorWithCustomLevel() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(createContainer("my-level"), null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    public void testHandleErrorWithErrorAndWarningOnly() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleError(createContainer(EventLevel.ERROR, EventLevel.WARNING), null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    public void testHandleNotSetup() throws IOException
    {
        Johnson503Filter johnson503Filter = new Johnson503Filter();
        final AtomicBoolean flushCalled = new AtomicBoolean(false);
        PrintWriter writer = new PrintWriter(new ByteArrayOutputStream())
        {
            public void flush()
            {
                flushCalled.set(true);
            }
        };
        MockControl responseMockCtrl = MockControl.createStrictControl(HttpServletResponse.class);

        HttpServletResponse response = (HttpServletResponse) responseMockCtrl.getMock();
        response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        responseMockCtrl.setVoidCallable();
        response.getWriter();
        responseMockCtrl.setReturnValue(writer);
        responseMockCtrl.replay();

        johnson503Filter.handleNotSetup(null, response);
        responseMockCtrl.verify();
        assertTrue(flushCalled.get());
    }

    private JohnsonEventContainer createContainer(final String... eventLevels) {
        final JohnsonEventContainer container = new JohnsonEventContainer();
        for (String eventLevel : eventLevels) {
            container.addEvent(createEvent(createEventLevel(eventLevel)));
        }
        return container;
    }

    private Event createEvent(final EventLevel eventLevel) {
        return new Event(DEFAULT_EVENT_TYPE,"event-desc", eventLevel);
    }

    private EventLevel createEventLevel(final String eventLevel) {
        return new EventLevel(eventLevel, "event-level-desc");
    }

}
