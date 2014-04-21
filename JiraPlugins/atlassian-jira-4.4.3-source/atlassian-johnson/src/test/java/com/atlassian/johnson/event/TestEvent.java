package com.atlassian.johnson.event;

import java.io.PrintStream;

import com.mockobjects.io.MockOutputStream;
import com.mockobjects.io.MockPrintStream;

import junit.framework.TestCase;

public class TestEvent extends TestCase
{
    public void testToStringDoesntSysout() throws Exception
    {
        // don't laugh, check the revision history...

        final PrintStream out = System.out;
        MockPrintStream mockOut = new MockPrintStream(new MockOutputStream());
        System.setOut(mockOut);
        try
        {
            Event event = new Event(new EventType("foo", "bar"), "fubar");
            mockOut.setExpectedPrintlnCalls(0);
            assertNotNull(event.toString());
            mockOut.verify();
        }
        finally
        {
            System.setOut(out);
        }
    }
}