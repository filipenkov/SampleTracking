/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.issue.index.job;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.local.AbstractIndexingTestCase;
import com.atlassian.jira.util.index.IndexLifecycleManager;

import com.mockobjects.dynamic.Mock;
import org.picocontainer.MutablePicoContainer;
import org.picocontainer.PicoContainer;

public class TestOptimizeIndexJob extends AbstractIndexingTestCase
{
    public TestOptimizeIndexJob(String s)
    {
        super(s);
    }

    public void testRunSuccessful() throws Exception
    {
        _testRun(new Long(100L));
    }

    public void testRunUnsuccessful() throws Exception
    {
        // This could happen if e.g. the index was locked.
        _testRun(new Long(-1L));
    }

    private void _testRun(Long optimizeResult) throws Exception
    {
        IndexLifecycleManager oldIndexManager = ComponentManager.getInstance().getIndexLifecycleManager();

        Mock mockIndexManager = new Mock(IndexLifecycleManager.class);
        mockIndexManager.setStrict(true);
        mockIndexManager.expectAndReturn("optimize", optimizeResult);

        try
        {
            registerInstanceInContainer(mockIndexManager.proxy());

            OptimizeIndexJob job = new OptimizeIndexJob();
            job.execute(null);

            mockIndexManager.verify();
        }
        finally
        {
            // Put the old manager back
            if (oldIndexManager != null)
            {
                registerInstanceInContainer(oldIndexManager);
            }
        }
    }

    private void registerInstanceInContainer(Object mockIndexManager)
    {
        PicoContainer container = ComponentManager.getInstance().getContainer();
        MutablePicoContainer mutableContainer = (MutablePicoContainer) container;
        //unregister current implemenation (if one exists) - prevent duplicate registration exception
        mutableContainer.unregisterComponent(IndexLifecycleManager.class);
        mutableContainer.registerComponentInstance(IndexLifecycleManager.class, mockIndexManager);
    }
}
