package com.atlassian.core.logging;

import junit.framework.TestCase;
import org.apache.log4j.Category;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LoggingEvent;

import java.util.List;

public class TestThreadLocalErrorCollection extends TestCase
{
    private static final Category log = Category.getInstance(TestThreadLocalErrorCollection.class);
    public static final LoggingEvent TEST_LOGGING_EVENT = new LoggingEvent("Test logging event", log, Priority.DEBUG, "test log message", new Exception());


    protected void setUp() throws Exception
    {
        ThreadLocalErrorCollection.enable();
    }

    public void tearDown() throws Exception
    {
        super.tearDown();
        ThreadLocalErrorCollection.clear();
        ThreadLocalErrorCollection.disable();
    }

    public void testDisabledPreventsAddingErrorMessages() throws Exception
    {
        ThreadLocalErrorCollection.disable();
        ThreadLocalErrorCollection.add(1, TEST_LOGGING_EVENT);
        assertTrue(ThreadLocalErrorCollection.isEmpty());
    }

    public void testEnabledAllowsErrorMessages() throws Exception
    {
        ThreadLocalErrorCollection.add(1, TEST_LOGGING_EVENT);
        assertFalse(ThreadLocalErrorCollection.isEmpty());
    }

    public void testToggle() throws Exception
    {
        testDisabledPreventsAddingErrorMessages();
        ThreadLocalErrorCollection.enable();
        testEnabledAllowsErrorMessages();
    }

    public void testLimitNormal() throws Exception
    {
        buildErrCollection(5);
        List errorEvents = ThreadLocalErrorCollection.getList();
        assertEquals(5, errorEvents.size());

        assertLatestEntry(errorEvents, 0);
    }

    private void assertLatestEntry(List errorEvents, int latestEntryNo)
    {
        DatedLoggingEvent dle = (DatedLoggingEvent) errorEvents.get(0);
        LoggingEvent le = dle.getEvent();
        assertEquals("ThreadLocalErrorCollection is not keep latest entries", String.valueOf(latestEntryNo), le.getMessage());
    }

    private void buildErrCollection(int x) throws Exception
    {
        LoggingEvent ev;
        for (int i = 0; i < x; i++)
        {
            ev = new LoggingEvent(i + "", log, Priority.DEBUG, i + "", new Exception());

            ThreadLocalErrorCollection.add(i, ev);
        }
    }


    public void testLimitDefault() throws Exception
    {
        buildErrCollection(101);

        List errorEvents = ThreadLocalErrorCollection.getList();

        assertEquals("ThreadLocalErrorCollection is not limiting its size", ThreadLocalErrorCollection.DEFAULT_LIMIT, errorEvents.size());
        assertLatestEntry(errorEvents, 1);
    }

    public void testLimitMutated() throws Exception
    {
        ThreadLocalErrorCollection.setLimit(2);

        buildErrCollection(101);

        List errorEvents = ThreadLocalErrorCollection.getList();

        assertEquals("ThreadLocalErrorCollection is not limiting its size", 2, errorEvents.size());
        assertLatestEntry(errorEvents, 99);
    }
}
