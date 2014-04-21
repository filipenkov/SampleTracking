package com.atlassian.jira.bc.workflow;

import com.atlassian.core.test.util.DuckTypeProxy;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.DescriptorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

@RunWith(ListeningMockitoRunner.class)
public class TestDefaultWorkflowServiceConcurrency
{
    private I18nHelper mockI18nHelper;

    @Mock private WorkflowManager mockWorkflowManager;
    @Mock private PermissionManager mockPermissionManager;

    @Mock private JiraWorkflow mockJiraWorkflow;
    @Mock private JiraWorkflow mockDraftWorkflow;


    @Before
    public void setUp() throws Exception
    {
        mockI18nHelper = new MockI18nHelper();
    }

    @Test
    public void testConcurrentEditAndOverwrite() throws Exception
    {
        final AtomicLong updateWorkflowTime = new AtomicLong();
        final AtomicLong overwriteWorkflowTime = new AtomicLong();
        final Object workflowManagerDelegate = new Object()
        {
            public void overwriteActiveWorkflow(final String username, final String workflowName)
            {
                assertEquals("testuser", username);
                assertEquals("jiraworkflow", workflowName);
                overwriteWorkflowTime.set(System.currentTimeMillis());
            }

            public void updateWorkflow(final String username, final JiraWorkflow workflow)
            {
                updateWorkflowTime.set(System.currentTimeMillis());
            }
        };

        final WorkflowManager mockWorkflowManager = (WorkflowManager) DuckTypeProxy.getProxy(WorkflowManager.class, workflowManagerDelegate);
        final CountDownLatch validateOverwriteLatch = new CountDownLatch(1);

        final DefaultWorkflowService defaultWorkflowService = new DefaultWorkflowService(mockWorkflowManager, null, null)
        {
            I18nHelper getI18nBean()
            {
                return mockI18nHelper;
            }

            boolean hasAdminPermission(final JiraServiceContext jiraServiceContext)
            {
                return true;
            }

            public void validateOverwriteWorkflow(final JiraServiceContext jiraServiceContext, final String workflowName)
            {
                //countdown the latch, to get the update going.
                validateOverwriteLatch.countDown();
                try
                {
                    //make this thread sleep for a little while to give the update thread some time
                    //to try to run.
                    Thread.sleep(200);
                }
                catch (final InterruptedException e)
                {
                    throw new RuntimeException(e);
                }
            }
        };
        final User testUser = new MockUser("testuser");
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        final JiraServiceContext jiraServiceContext = new JiraServiceContextImpl(testUser, errorCollection);

        final List<Callable<Object>> tasks = Lists.newArrayList();
        when(mockJiraWorkflow.getDescriptor()).thenReturn(new DescriptorFactory().createWorkflowDescriptor());
        when(mockJiraWorkflow.isEditable()).thenReturn(true);
        tasks.add(new Callable<Object>()
        {
            public Object call() throws Exception
            {
                //wait until validate was called.
                validateOverwriteLatch.await();
                defaultWorkflowService.updateWorkflow(jiraServiceContext, mockJiraWorkflow);
                return null;
            }
        });
        tasks.add(new Callable<Object>()
        {

            public Object call() throws Exception
            {
                defaultWorkflowService.overwriteActiveWorkflow(jiraServiceContext, "jiraworkflow");
                return null;
            }
        });

        runMultiThreadedTest(tasks, 2);

        //check that the update of the workflow always occurs after the overwrite.
        assertTrue(updateWorkflowTime.get() >= overwriteWorkflowTime.get());
    }

    private void runMultiThreadedTest(final List<Callable<Object>> tasks, final int threads) throws InterruptedException, ExecutionException
    {
        final ExecutorService pool = Executors.newFixedThreadPool(threads);
        try
        {
            runWithExecutor(tasks, pool);
        }
        finally
        {
            pool.shutdown();
        }
    }

    private void runWithExecutor(Collection<Callable<Object>> tasks, ExecutorService pool)
            throws InterruptedException, ExecutionException
    {
        List<Future<Object>> futures;
        try
        {
            futures = pool.invokeAll(tasks);
        }
        catch (final InterruptedException e)
        {
            throw new RuntimeException(e);
        }

        //wait until all tasks have finished executing.
        for (Future future : futures)
        {
            future.get();
        }
    }


}
