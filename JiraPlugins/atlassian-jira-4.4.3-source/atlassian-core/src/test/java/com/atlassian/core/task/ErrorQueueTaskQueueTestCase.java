package com.atlassian.core.task;

import junit.framework.TestCase;
import com.atlassian.core.task.*;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 25/03/2004
 * Time: 17:41:32
 * To change this template use File | Settings | File Templates.
 */
public class ErrorQueueTaskQueueTestCase  extends TestCase
{
    public void testTaskManager()
    {
        final boolean[] task2executed = new boolean[1];

        ErrorQueuedTaskQueue queue = new ErrorQueuedTaskQueue();

        queue.setRetryCount(2);
        DefaultTaskManager manager = new DefaultTaskManager(queue);
        Task task1 = new Task(){
            public void execute() throws Exception { throw new Exception(); }
        };

        Task task2 = new Task(){
            public void execute() throws Exception { task2executed[0] = true; }
        };

        manager.addTask(task1);
        manager.addTask(task2);

        assertNotNull(manager.getTaskQueue());
        assertEquals(2, manager.getTaskQueue().size());
        manager.flush();
        assertEquals(1, queue.size());
        assertEquals(0, queue.getErrorQueue().size());
        manager.flush();
        assertEquals(1, queue.size());
        assertEquals(0, queue.getErrorQueue().size());
        manager.flush();
        assertTrue(task2executed[0]);
        assertEquals(0, queue.size());
        assertEquals(1, queue.getErrorQueue().size());
    }
}