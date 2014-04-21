package com.atlassian.core.task;

import java.util.*;

/**
 * A TaskManager implementation that manages multiple queues
 */
public class DefaultMultiQueueTaskManager implements MultiQueueTaskManager
{
    private Map queues = new HashMap();

    public DefaultMultiQueueTaskManager(String queueName, TaskQueue queue)
    {
        addTaskQueue(queueName, queue);
    }

    public DefaultMultiQueueTaskManager(Map queues)
    {
        setTaskQueues(queues);
    }

    public void addTaskQueue(String name, TaskQueue queue)
    {
        if(queues.keySet().contains(name))
        {
            throw new IllegalArgumentException("The queue specified already exists in the task manager");
        }
        queues.put(name, queue);
    }

    public TaskQueue removeTaskQueue(String queueName, TaskQueue taskQueue, boolean flush)
    {
        TaskQueue queue = getTaskQueue(queueName);
        if(queue!=null && flush)
        {
            queue.flush();
        }
        return queue;
    }

    public TaskQueue getTaskQueue(String name)
    {
        return (TaskQueue)queues.get(name);
    }

    public void setTaskQueues(Map queues)
    {
        Map.Entry entry;
        for (Iterator iterator = queues.entrySet().iterator(); iterator.hasNext();)
        {
            entry = (Map.Entry)iterator.next();
            addTaskQueue(entry.getKey().toString(), (TaskQueue)entry.getValue());
        }
    }

    public void addTask(String queueName, Task task)
    {
        getTaskQueue(queueName).addTask(task);
    }

    public void flush(String queueName)
    {
        getTaskQueue(queueName).flush();
    }

    public void flush()
    {
        for (Iterator iterator = queues.values().iterator(); iterator.hasNext();)
        {
            ((TaskQueue)iterator.next()).flush();
        }
    }
}
