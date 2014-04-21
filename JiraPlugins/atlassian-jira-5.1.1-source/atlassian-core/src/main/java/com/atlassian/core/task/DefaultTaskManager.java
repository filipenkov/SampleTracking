package com.atlassian.core.task;



/**
 * Default implementation of a TaskManager.  Suitable for staight forward usage.
 *
 * @author Ross Mason
 */
public class DefaultTaskManager implements TaskManager
{
    private TaskQueue taskQueue;

    public DefaultTaskManager(TaskQueue queue)
    {
        setTaskQueue(queue);
    }

    public TaskQueue getTaskQueue()
    {
        return taskQueue;
    }

    public void setTaskQueue(TaskQueue taskQueue)
    {
        this.taskQueue = taskQueue;
    }

    public void addTask(Task task)
    {
        if(task == null) return;
        taskQueue.addTask(task);
    }

    public void flush()
    {
        if(taskQueue != null) {
            taskQueue.flush();
        }
    }
}
