package com.atlassian.core.task;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: ROSS
 * Date: 25/03/2004
 * Time: 16:15:44
 * To change this template use File | Settings | File Templates.
 */
public interface MultiQueueTaskManager
{
    TaskQueue getTaskQueue(String queueName);

    void addTaskQueue(String queueName, TaskQueue taskQueue);

    TaskQueue removeTaskQueue(String queueName, TaskQueue taskQueue, boolean flush);

    void setTaskQueues(Map queues);

    void addTask(String queueName, Task task);

    void flush(String queueName);

    void flush();
}
