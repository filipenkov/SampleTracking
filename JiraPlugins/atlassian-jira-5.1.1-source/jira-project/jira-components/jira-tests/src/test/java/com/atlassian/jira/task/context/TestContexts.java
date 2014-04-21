package com.atlassian.jira.task.context;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.security.auth.trustedapps.MockI18nHelper;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.collect.Sized;

import org.apache.log4j.Logger;

import com.atlassian.jira.local.ListeningTestCase;

/** @since v3.13 */
public class TestContexts extends ListeningTestCase
{
    @Test
    public void testCreateWithNullLogger()
    {
        try
        {
            Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(), null, "Test Message");
            fail("Null not acceptable for logger.");
        }
        catch (final Exception ignore)
        {}
    }

    @Test
    public void testCreateWithNullI18n()
    {
        try
        {
            Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, null, Logger.getLogger(TestContexts.class), "Test Message");
            fail("Null not acceptable for mock bean.");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testCreateWithNullSink()
    {
        try
        {
            Contexts.percentageReporter(new Size(3), null, new MockI18nHelper(), Logger.getLogger(TestContexts.class), "Test Message");
            fail("Null not acceptable for sink.");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testCreateWithZeroTasks()
    {
        assertEquals(UnboundContext.class, Contexts.percentageReporter(new Size(0), TaskProgressSink.NULL_SINK, new MockI18nHelper(),
            Logger.getLogger(TestContexts.class), "Test Message").getClass());
    }

    @Test
    public void testCreateWithNullEvent()
    {
        try
        {
            Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(), Logger.getLogger(TestContexts.class),
                "Test Message", null);
            fail("Null not acceptable for event.");
        }
        catch (final IllegalArgumentException ignore)
        {}
    }

    @Test
    public void testCreateWorksWithEvent()
    {
        Contexts.percentageReporter(new Size(3), TaskProgressSink.NULL_SINK, new MockI18nHelper(), Logger.getLogger(TestContexts.class),
            "Test Message", new MockJohnsonEvent());
    }

    @Test
    public void testSetProgress()
    {
        final CountedLogger countLogger = new CountedLogger(getClass().getName());
        final CountedTaskProgressSink countedSink = new CountedTaskProgressSink();

        final Context context = Contexts.percentageReporter(new Size(10), countedSink, new MockI18nHelper(), countLogger, "Test Message",
            new MockJohnsonEvent());

        int cnt = 0;

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());

        context.start(null).complete();
        cnt++;
        assertEquals(cnt, countLogger.getInfoCalledCount());
        assertEquals(cnt, countedSink.getMakeProgressCount());
    }

    private static class CountedLogger extends Logger
    {
        private int infoCalledCount = 0;

        protected CountedLogger(final String s)
        {
            super(s);
        }

        @Override
        public void info(final Object object)
        {
            infoCalledCount++;
        }

        @Override
        public void info(final Object object, final Throwable throwable)
        {
            infoCalledCount++;
        }

        public int getInfoCalledCount()
        {
            return infoCalledCount;
        }
    }

    private static class CountedTaskProgressSink implements TaskProgressSink
    {
        private int makeProgressCount = 0;

        public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
        {
            makeProgressCount++;
        }

        public int getMakeProgressCount()
        {
            return makeProgressCount;
        }
    }

    private class Size implements Sized
    {
        private final int size;

        public Size(final int size)
        {
            this.size = size;
        }

        public int size()
        {
            return size;
        }

        public boolean isEmpty()
        {
            return size == 0;
        }
    }
}
