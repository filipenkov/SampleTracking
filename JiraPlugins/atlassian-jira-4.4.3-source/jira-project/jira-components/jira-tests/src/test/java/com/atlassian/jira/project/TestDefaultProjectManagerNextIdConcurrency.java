package com.atlassian.jira.project;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.Test;
import static org.junit.Assert.*;

import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.concurrent.Latch;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

public class TestDefaultProjectManagerNextIdConcurrency extends ListeningTestCase
{
    @Test
    public void testNextId() throws Throwable
    {
        final int THREADS = 100;
        final Latch running = new Latch(THREADS);
        final MockOfBizDelegator delegator = new MockOfBizDelegator(CollectionBuilder.newBuilder(newProjectGV()).asList(),
            new ArrayList<GenericValue>());
        final AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();
        final DefaultProjectManager manager = new DefaultProjectManager(delegator, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public long getNextId(final GenericValue project)
            {
                running.countDown();
                running.await();
                return super.getNextId(project);
            }
        };

        final Latch complete = new Latch(THREADS);
        final List<Long> results = new Vector<Long>();

        for (int i = 0; i < THREADS; i++)
        {
            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        results.add(manager.getNextId(newProjectGV()));
                    }
                    catch (final Throwable ex)
                    {
                        throwable.compareAndSet(null, ex);
                    }
                    finally
                    {
                        complete.countDown();
                    }
                }
            }).start();
        }

        complete.await();
        if (throwable.get() != null)
        {
            throw throwable.get();
        }
        assertEquals(THREADS, results.size());
        assertEquals(THREADS, new HashSet<Long>(results).size());
        assertEquals((Long) (THREADS + 1L), manager.getProject(1L).getLong("counter"));
    }

    private MockGenericValue newProjectGV()
    {
        return new MockGenericValue("Project", new PrimitiveMap.Builder().add("id", 1L).add("counter", 1L).toMap());
    }
}
