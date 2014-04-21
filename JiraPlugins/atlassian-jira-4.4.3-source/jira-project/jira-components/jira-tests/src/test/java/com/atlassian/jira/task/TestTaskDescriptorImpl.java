package com.atlassian.jira.task;

import com.atlassian.jira.user.MockCrowdService;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.jira.MockProviderAccessor;
import com.opensymphony.user.ProviderAccessor;
import com.opensymphony.user.User;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Collection;

/**
 * @since v3.13
 */

public class TestTaskDescriptorImpl extends ListeningTestCase
{
    private User testUser;
    private static final String PROGRESS_URL_TASK_ID = "progressURL?taskId=";
    private static final Long TASK_ID = new Long(1);
    private static final String TASK_DESC = "My Description";

    @Before
    public void setUp() throws Exception
    {
        final ProviderAccessor providerAccessorProxy = new MockProviderAccessor();
        testUser = new User("TestTaskDescriptorImplUser", providerAccessorProxy, new MockCrowdService());
    }

    private class OurTaskContext implements TaskContext
    {
        public String buildProgressURL(Long taskId)
        {
            return PROGRESS_URL_TASK_ID + taskId;
        }
    }

    @Test
    public void testMainConstructor()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();

        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser, taskProgressIndicator);

        assertEquals(TASK_ID, taskDesc.getTaskId());
        assertEquals(TASK_DESC, taskDesc.getDescription());
        assertEquals(testUser, taskDesc.getUser());
        assertEquals(taskProgressIndicator, taskDesc.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskDesc.getTaskContext());
        assertNotNull(taskDesc.getSubmittedTimestamp());
        assertNull(taskDesc.getStartedTimestamp());
        assertNull(taskDesc.getFinishedTimestamp());
        assertFalse(taskDesc.isStarted());
        assertFalse(taskDesc.isFinished());

        assertEquals(0, taskDesc.getElapsedRunTime());
        assertEquals(PROGRESS_URL_TASK_ID + taskDesc.getTaskId(), taskDesc.getProgressURL());

    }

    @Test
    public void testMainConstructorNullTaskId()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        // test exception throwing
        try
        {
            new TaskDescriptorImpl(null, TASK_DESC, ourTaskContext, testUser, taskProgressIndicator);
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testMainConstructorNullTaskDescriptor()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, null, ourTaskContext, testUser, taskProgressIndicator);
            fail("Expecting IllegalArgumentException");
        }
        catch (IllegalArgumentException e)
        {
        }
    }

    @Test
    public void testMainConstructorNullUser()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, null, taskProgressIndicator);
        }
        catch (IllegalArgumentException e)
        {
            fail("User is optional");
        }
    }

    @Test
    public void testMainConstructorNullTaskIndicator()
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser, null);
        }
        catch (IllegalArgumentException e)
        {
            fail("TaskProgressInformation should be optional");
        }

    }

    @Test
    public void testMainConstructorNullTaskContext()
    {
        TaskProgressIndicator taskProgressIndicator = new NoOpTaskProgressIndicator();
        try
        {
            new TaskDescriptorImpl(TASK_ID, TASK_DESC, null, testUser, taskProgressIndicator);
            fail("TaskContext should not be optional");
        }
        catch (IllegalArgumentException e)
        {
            //expected.
        }

    }

    @Test
    public void testTaskDescriptorImpl()
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        // try the case where we have null TaskContext
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, null, null);

        assertEquals(TASK_ID, taskDesc.getTaskId());
        assertEquals(TASK_DESC, taskDesc.getDescription());
        assertNull(taskDesc.getUser());
        assertNull(taskDesc.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskDesc.getTaskContext());
        assertEquals(PROGRESS_URL_TASK_ID + taskDesc.getTaskId(), taskDesc.getProgressURL());

        assertNotNull(taskDesc.getSubmittedTimestamp());
        assertNull(taskDesc.getStartedTimestamp());
        assertNull(taskDesc.getFinishedTimestamp());
        assertFalse(taskDesc.isStarted());
        assertFalse(taskDesc.isFinished());

        assertEquals(0, taskDesc.getElapsedRunTime());

        // test that finished cant be set before started
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not allow finsihed to be set before started");
        }
        catch (IllegalStateException e)
        {
            // expected
        }
    }

    @Test
    public void testTaskDescriptorImplContructorNull() throws Exception
    {
        try
        {
            new TaskDescriptorImpl(null);
            fail("Should not allow null params");
        }
        catch (IllegalArgumentException e)
        {
            // expected
        }
    }

    @Test
    public void testCopyContructor() throws Exception
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser, null);

        TaskDescriptorImpl taskClone = new TaskDescriptorImpl(taskDesc);

        assertEquals(taskDesc.getTaskId(), taskClone.getTaskId());
        assertEquals(taskDesc.getDescription(), taskClone.getDescription());
        assertSame(taskDesc.getUser(), taskClone.getUser());
        assertNull(taskClone.getTaskProgressIndicator());
        assertSame(ourTaskContext, taskClone.getTaskContext());
        assertEquals(taskDesc.getProgressURL(), taskClone.getProgressURL());

        assertEquals(taskDesc.getSubmittedTimestamp(), taskClone.getSubmittedTimestamp());
        assertEquals(taskDesc.getStartedTimestamp(), taskClone.getStartedTimestamp());
        assertEquals(taskDesc.getFinishedTimestamp(), taskClone.getFinishedTimestamp());
        assertEquals(taskDesc.isStarted(), taskClone.isStarted());
        assertEquals(taskDesc.isFinished(), taskClone.isFinished());

        assertEquals(taskDesc.getElapsedRunTime(), taskClone.getElapsedRunTime());

        // check that mutable stuff to one does not affect the clone
        taskDesc.setStartedTimestamp();

        assertFalse(taskClone.isStarted());
        assertNull(taskClone.getStartedTimestamp());

        assertFalse(taskClone.isFinished());
        assertNull(taskClone.getFinishedTimestamp());

        taskDesc.setFinishedTimestamp();

        assertFalse(taskClone.isStarted());
        assertNull(taskClone.getStartedTimestamp());

        assertFalse(taskClone.isFinished());
        assertNull(taskClone.getFinishedTimestamp());
    }

    @Test
    public void testGetElapsedTime() throws Exception
    {
        OurTaskContext ourTaskContext = new OurTaskContext();
        TaskDescriptorImpl taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser, null);

        //there should be no elapsed time when started.
        long oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);
        oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);
        oldElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(0, oldElapsedTime);

        //elapsed time should increase while the task is started but not finished.
        taskDesc.setStartedTimestamp();
        long newElapsedTime = taskDesc.getElapsedRunTime();
        assertTrue(oldElapsedTime <= newElapsedTime);
        oldElapsedTime = newElapsedTime;

        //once finished, the elapsed time should remain static.
        taskDesc.setFinishedTimestamp();
        newElapsedTime = taskDesc.getElapsedRunTime();
        assertTrue(oldElapsedTime <= newElapsedTime);
        oldElapsedTime = newElapsedTime;

        newElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(oldElapsedTime, newElapsedTime);
        newElapsedTime = taskDesc.getElapsedRunTime();
        assertEquals(oldElapsedTime, newElapsedTime);

        taskDesc = new TaskDescriptorImpl(TASK_ID, TASK_DESC, ourTaskContext, testUser, null);
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not be able to set the finished time before it is started.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        taskDesc.setStartedTimestamp();
        try
        {
            taskDesc.setStartedTimestamp();
            fail("Should not be able to set the started time twice.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }

        taskDesc.setFinishedTimestamp();
        try
        {
            taskDesc.setFinishedTimestamp();
            fail("Should not be able to set the finished time twice.");
        }
        catch (IllegalStateException e)
        {
            //expected.
        }
    }

    class NoOpTaskProgressIndicator implements TaskProgressIndicator
    {
        public void addListener(TaskProgressListener listener)
        {
        }

        public void removeListener (TaskProgressListener listener)
        {
        }

        public Collection /*TaskProgressEvent*/ getLastProgressEvents()
        {
            return null;
        }

        public TaskProgressEvent getLastProgressEvent()
        {
            return null;
        }
    }
}
