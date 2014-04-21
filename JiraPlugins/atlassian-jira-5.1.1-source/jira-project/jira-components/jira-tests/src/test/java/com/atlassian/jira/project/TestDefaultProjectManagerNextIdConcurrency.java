package com.atlassian.jira.project;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.concurrent.Latch;
import org.junit.Before;
import org.junit.Test;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class TestDefaultProjectManagerNextIdConcurrency extends ListeningTestCase
{
    private final MockOfBizDelegator mockDelegator = new MockOfBizDelegator(CollectionBuilder.newBuilder(newProjectGV()).asList(),
            new ArrayList<GenericValue>());

    @Before
    public void setup()
    {
        ComponentAccessor.initialiseWorker((new MockComponentWorker().addMock(OfBizDelegator.class, mockDelegator)));
    }

    @Test
    public void testNextId() throws Throwable
    {
        final int THREADS = 100;
        final Latch running = new Latch(THREADS);
        final AtomicReference<Throwable> throwable = new AtomicReference<Throwable>();
        final DefaultProjectManager manager = new DefaultProjectManager(mockDelegator, null, null, null, null, null, null, null, null, null)
        {
            @Override
            public long getNextId(final Project project)
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
                        results.add(manager.getNextId(newProject()));
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

    private MockProject newProject()
    {
        return new MockProject(1L);
    }
}
