package com.atlassian.johnson.filters;

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
    public void testHandleError() throws IOException
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

}
