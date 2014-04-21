package com.atlassian.core.task;

/**
 * Defines the api for a common task manager used for queue tasks to be executed
 * later
 *
 * @author Ross Mason
 */
public interface TaskManager
{
    TaskQueue getTaskQueue();

    void setTaskQueue(TaskQueue taskQueue);

    void addTask(Task task);

    void flush();
}
