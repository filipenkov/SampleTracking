package com.atlassian.jira.index;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.index.Index.Result;
import com.atlassian.jira.util.RuntimeInterruptedException;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.AssertionFailedError;
import com.atlassian.jira.local.ListeningTestCase;

public class TestCompositeResultBuilder extends ListeningTestCase
{
    @Test
    public void testNonNullResult() throws Exception
    {
        assertNotNull(new CompositeResultBuilder().add(new MockResult()).toResult());
    }

    @Test
    public void testNullResultNotAllowed() throws Exception
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        try
        {
            builder.add(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullCompletionTaskNotAllowed() throws Exception
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        try
        {
            builder.addCompletionTask(null);
            fail("IllegalArgumentException expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testResultThatCallsInnerResultAwait() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public void await()
            {
                count.getAndIncrement();
            }
        });
        builder.toResult().await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatCallsInnerResultAwaitWithTimeout() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                count.getAndIncrement();
                return true;
            }
        });
        assertTrue(builder.toResult().await(100, TimeUnit.SECONDS));
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatCallsInnerResultIsDone() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean isDone()
            {
                count.getAndIncrement();
                return true;
            }
        });
        assertTrue(builder.toResult().isDone());
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatReturnNotDoneIfInnerNotDone() throws Exception
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean isDone()
            {
                return false;
            }
        });
        assertFalse(builder.toResult().isDone());
    }

    @Test
    public void testResultThatCallsCompletionTask() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                count.getAndIncrement();
            }
        };
        builder.addCompletionTask(completionTask);
        builder.toResult().await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultCallsCompletionTasksOnlyOnce() throws Exception
    {
        final AtomicInteger count = new AtomicInteger();
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                count.getAndIncrement();
            }
        };
        builder.addCompletionTask(completionTask);
        final Result result = builder.toResult();
        result.await();
        assertEquals(1, count.get());
        result.await();
        assertEquals(1, count.get());
    }

    @Test
    public void testResultThatDoesNotCallCompletionTaskIfInterrupted() throws Exception
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public void await()
            {
                throw new RuntimeInterruptedException(new InterruptedException());
            }
        });
        final Runnable completionTask = new Runnable()
        {
            ///CLOVER:OFF
            public void run()
            {
                throw new AssertionFailedError("should not be called");
            }
            ///CLOVER:ON

        };
        builder.addCompletionTask(completionTask);
        try
        {
            builder.toResult().await();
            fail("RuntimeInterruptedException expected");
        }
        catch (final RuntimeInterruptedException expected)
        {}
    }

    @Test
    public void testResultThatDoesNotCallCompletionTaskIfAwaitTimeoutFails() throws Exception
    {
        final CompositeResultBuilder builder = new CompositeResultBuilder();
        builder.add(new MockResult()
        {
            @Override
            public boolean await(final long timeout, final TimeUnit unit)
            {
                return false;
            }
        });
        final Runnable completionTask = new Runnable()
        {
            public void run()
            {
                throw new AssertionFailedError("should not be called");
            }
        };
        builder.addCompletionTask(completionTask);
        builder.toResult().await(100, TimeUnit.MILLISECONDS);
    }
}
