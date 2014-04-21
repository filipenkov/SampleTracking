package com.atlassian.core.task;

import org.apache.commons.collections.Buffer;

import java.sql.Timestamp;
import java.util.Collection;

import com.atlassian.core.task.Task;

/**
 * Represents a queue of Task objects.  Arbitary task objects can be queue and then flushed when necessary.
 * The flush will cause all tasks in the queue to execute.
 * @see com.atlassian.core.task.Task
 *
 * @author Ross Mason
 */
public interface TaskQueue
{
    /**
     * Will execute every task in the queue
     */
    void flush();

    /**
     * Obtains the current size of the queue
     * @return the queue size
     */
    int size();

    /**
     * Adds a task to the end of the queue
     * @param task the task to add
     */
    void addTask(Task task);

    /**
     * Returns true if the queue is currently flushing or false otherwise
     * @return true if the queue is currently flushing or false otherwise
     */
    boolean isFlushing();

    /**
     * Obtains the time when the queue started flushing. This returns null
     * if the queue is not being flushed
     * @return the time when the queue started flushing
     */
    Timestamp getFlushStarted();

    /**
     * Throw away all the tasks in the queue
     */
    void clear();

    /**
     * Get a Collection of the Tasks currently in the queue
     */
    Collection getTasks();
}
