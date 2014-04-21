package com.atlassian.jira.task;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Date;

/**
 * Test for the {@link com.atlassian.jira.task.TaskProgressEvent} class.
 *
 * @since v3.13
 */

public class TestTaskProgressEvent extends ListeningTestCase
{
    private static final Long TASK_ID = new Long(1);

    @Test
    public void testTaskProcessEvent()
    {
        String message = "MY test event";
        String subTask = "Sub task";
        long elapsedTime = 102L;
        long progress = 99;
        Date startDate = new Date();

        TaskProgressEvent event = new TaskProgressEvent(TASK_ID, elapsedTime, progress, subTask, message);

        assertEquals(TASK_ID, event.getTaskId());
        assertEquals(message, event.getMessage());
        assertEquals(subTask, event.getCurrentSubTask());
        assertEquals(elapsedTime, event.getElapsedRunTime());
        assertEquals(progress, event.getTaskProgress());

        //make sure it is created after the test started.
        assertTrue(event.getCreationTimeStamp().compareTo(startDate) >= 0);
    }
}
