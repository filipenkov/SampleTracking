package com.atlassian.core.task;

import com.atlassian.core.task.DefaultTaskManager;
import com.atlassian.core.task.DefaultTaskQueue;
import com.atlassian.core.task.Task;
import com.atlassian.core.task.TaskQueue;
import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 25/03/2004
 * Time: 12:08:27
 * To change this template use File | Settings | File Templates.
 */
public class TaskManagerTestCase extends TestCase
{
    public void testTaskManager()
    {
        final boolean[] task1executed = new boolean[1];
        final boolean[] task2executed = new boolean[1];

        TaskQueue queue = new DefaultTaskQueue();
        DefaultTaskManager manager = new DefaultTaskManager(queue);
        Task task1 = new Task(){
            public void execute() throws Exception { task1executed[0] = true; }
        };

        Task task2 = new Task(){
            public void execute() throws Exception { task2executed[0] = true; }
        };

        manager.addTask(task1);
        manager.addTask(task2);

        assertNotNull(manager.getTaskQueue());
        assertEquals(2, manager.getTaskQueue().size());
        manager.flush();
        assertNull(manager.getTaskQueue().getFlushStarted());
        assertTrue(task1executed[0]);
        assertTrue(task2executed[0]);

    }
}
