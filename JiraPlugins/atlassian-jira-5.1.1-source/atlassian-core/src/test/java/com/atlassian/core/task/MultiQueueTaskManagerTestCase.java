package com.atlassian.core.task;

import com.atlassian.core.task.*;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 25/03/2004
 * Time: 16:26:41
 * To change this template use File | Settings | File Templates.
 */
public class MultiQueueTaskManagerTestCase extends TestCase
{
    public void testTaskManager()
    {
        final boolean[] task1executed = new boolean[1];
        final boolean[] task2executed = new boolean[1];
        final boolean[] task3executed = new boolean[1];

        TaskQueue queue1 = new DefaultTaskQueue();
        TaskQueue queue2 = new DefaultTaskQueue();
        Map queues = new HashMap();
        queues.put("queue1", queue1);
        queues.put("queue2", queue2);

        DefaultMultiQueueTaskManager manager = new DefaultMultiQueueTaskManager(queues);
        Task task1 = new Task(){
            public void execute() throws Exception { task1executed[0] = true; }
        };

        Task task2 = new Task(){
            public void execute() throws Exception { task2executed[0] = true; }
        };

        Task task3 = new Task(){
            public void execute() throws Exception { task3executed[0] = true; }
        };

        manager.addTask("queue1", task1);
        manager.addTask("queue1", task2);
        manager.addTask("queue2", task3);

        assertNotNull(manager.getTaskQueue("queue1"));
        assertNotNull(manager.getTaskQueue("queue2"));
        assertEquals(2, manager.getTaskQueue("queue1").size());
        assertEquals(1, manager.getTaskQueue("queue2").size());
        manager.flush();

        assertTrue(task1executed[0]);
        assertTrue(task2executed[0]);
        assertTrue(task3executed[0]);

    }
}