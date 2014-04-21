package com.atlassian.jira.index;

import com.atlassian.jira.index.Index.UpdateMode;
import com.atlassian.jira.index.QueueingIndex.CompositeOperation;
import com.atlassian.jira.index.QueueingIndex.FutureOperation;
import com.atlassian.jira.index.QueueingIndex.Task;
import com.atlassian.jira.local.ListeningTestCase;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestQueueingIndex extends ListeningTestCase
{
    @Test
    public void testNewSearcherAfterCreate() throws Exception
    {
        final AtomicBoolean ran = new AtomicBoolean();
        final MockOperation operation = new MockOperation()
        {
            @Override
            void perform(final Writer writer) throws IOException
            {
                ran.set(true);
            }
        };
        final Index index = new QueueingIndex("TestQueueingIndex", new MockCloseableIndex());
        index.perform(operation).await();
        assertTrue(ran.get());
    }

    @Test
    public void testThreadLifeCycle() throws Exception
    {
        final AtomicReference<Thread> thread = new AtomicReference<Thread>();
        final AtomicBoolean closed = new AtomicBoolean();
        final CloseableIndex index = new QueueingIndex(new ThreadFactory()
        {
            public Thread newThread(final Runnable r)
            {
                thread.set(new Thread(r));
                return thread.get();
            }
        }, new MockCloseableIndex()
        {
            @Override
            public void close()
            {
                closed.set(true);
            }
        });
        index.perform(new MockOperation());
        assertNotNull("thread should be created", thread.get());
        assertTrue("thread should be alive", thread.get().isAlive());
        index.close();
        assertTrue("delegate should be closed", closed.get());
        assertFalse("thread should be dead", thread.get().isAlive());

        // try again to make sure process is repeatable...
        thread.set(null);
        closed.set(false);

        index.perform(new MockOperation());
        assertNotNull("thread should be created", thread.get());
        assertTrue("thread should be alive", thread.get().isAlive());
        index.close();
        assertTrue("delegate should be closed", closed.get());
        assertFalse("thread should be dead", thread.get().isAlive());
    }

    @Test
    public void testThreadDeath() throws Exception
    {
        final AtomicReference<Thread> thread = new AtomicReference<Thread>();
        final AtomicBoolean closed = new AtomicBoolean();
        final CloseableIndex index = new QueueingIndex(new ThreadFactory()
        {
            public Thread newThread(final Runnable r)
            {
                thread.set(new Thread(r));
                return thread.get();
            }
        }, new MockCloseableIndex()
        {
            @Override
            public void close()
            {
                closed.set(true);
            }
        });
        index.perform(new MockOperation());
        assertNotNull("thread should be created", thread.get());
        assertTrue("thread should be alive", thread.get().isAlive());
        thread.get().interrupt();
        thread.get().join();
        assertFalse("delegate should not be closed", closed.get());
        assertFalse("thread should be dead", thread.get().isAlive());

        // try again to make sure process is repeatable...
        thread.set(null);

        index.perform(new MockOperation());
        assertNotNull("thread should be created", thread.get());
        assertTrue("thread should be alive", thread.get().isAlive());
        index.close();
        assertTrue("delegate should be closed", closed.get());
        assertFalse("thread should be dead", thread.get().isAlive());
    }

    @Test
    public void testTaskDrainsAll() throws Exception
    {
        final BlockingQueue<FutureOperation> queue = new LinkedBlockingQueue<FutureOperation>();
        final CloseableIndex delegate = new MockCloseableIndex();
        final QueueingIndex index = new QueueingIndex("TestQueueingIndex.testTaskDrainsAll", delegate);
        final Task task = index.new Task(queue);
        queue.add(new QueueingIndex.FutureOperation(new MockOperation()));
        queue.add(new QueueingIndex.FutureOperation(new MockOperation()));
        queue.add(new QueueingIndex.FutureOperation(new MockOperation()));
        queue.add(new QueueingIndex.FutureOperation(new MockOperation()));
        task.index();
        assertTrue("queue should be empty after run", queue.isEmpty());
    }

    @Test
    public void testTaskCompositeBatchModeOverrides() throws Exception
    {
        final List<FutureOperation> operations = Lists.newArrayList(new FutureOperation(new MockOperation()), new FutureOperation(new MockOperation(UpdateMode.BATCH)), new FutureOperation(new MockOperation()));
        final CompositeOperation operation = new CompositeOperation(operations);
        assertEquals(UpdateMode.BATCH, operation.mode());
    }

    @Test
    public void testTaskBlocksUntilOperationAvailable() throws Exception
    {
        final BlockingQueue<FutureOperation> queue = new LinkedBlockingQueue<FutureOperation>();
        final CloseableIndex delegate = new MockCloseableIndex();
        final CountDownLatch started = new CountDownLatch(1);
        final CountDownLatch finished = new CountDownLatch(1);
        final QueueingIndex index = new QueueingIndex("TestQueueingIndex.testTaskBlocksUntilOperationAvailable", delegate);
        try
        {
            final Task task = index.new Task(queue);

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        started.countDown();
                        task.index();
                    }
                    catch (final InterruptedException e)
                    {
                        throw new RuntimeException(e);
                    }
                    finished.countDown();
                }
            }).start();
            started.await();
            assertEquals(1, finished.getCount());
            Thread.sleep(20);
            assertEquals(1, finished.getCount());

            final MockOperation operation = new MockOperation();
            final FutureOperation futureOp = new FutureOperation(operation);
            queue.add(futureOp);
            finished.await();
            futureOp.get();
            assertTrue("queue should be empty after run", queue.isEmpty());
            assertTrue("operation should have been performed", operation.isPerformed());
        }
        finally
        {
            index.close();
        }
    }

    @Test
    public void testNullName()
    {
        try
        {
            new QueueingIndex((String) null, new MockCloseableIndex());
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullThreadFactory()
    {
        try
        {
            new QueueingIndex((ThreadFactory) null, new MockCloseableIndex());
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }

    @Test
    public void testNullIndex()
    {
        try
        {
            new QueueingIndex("blah", null);
            fail("IllegalArg expected");
        }
        catch (final IllegalArgumentException expected)
        {}
    }
}
