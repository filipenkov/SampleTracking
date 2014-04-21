package com.atlassian.core.task;

import org.apache.log4j.Logger;

import javax.mail.MessagingException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 26/04/2006
 * Time: 11:22:54
 * To change this template use File | Settings | File Templates.
 */
public class AbstractErrorQueuedTaskQueue extends AbstractTaskQueue implements TaskQueueWithErrorQueue
{
    private static final transient Logger log = Logger.getLogger(AbstractErrorQueuedTaskQueue.class);

    private TaskQueue errorQueue;

    private int retryCount = 5;

    private List failed;

    public AbstractErrorQueuedTaskQueue(TaskQueue errorQueue, FifoBuffer buffer)
    {
        super(buffer);
        this.errorQueue = errorQueue;
    }

    public void flush()
    {
        failed = new ArrayList();
        super.flush();
        for (Iterator iterator = failed.iterator(); iterator.hasNext();)
        {
            addTask((Task)iterator.next());
        }
    }

    protected void handleException(Task task, Exception rootException)
    {
        TaskDecorator theTask = (TaskDecorator)task;

        if (theTask.getExecutionCount() > retryCount) {

            errorQueue.addTask(theTask.getTask());
        }else {

            failed.add(task);
        }
        if (rootException instanceof MessagingException)
        {
            Exception e = rootException;
            while (e instanceof MessagingException)
            {
                MessagingException me = (MessagingException)e;
                log.error(me.getMessage(), me);
                e = me.getNextException();
            }
        }
        else
            log.error(rootException, rootException);
    }

    public void addTask(Task task)
    {
        if(task instanceof TaskDecorator)
        {
            super.addTask(task);
        } else {
            super.addTask(new TaskDecorator(task));
        }
    }

    public TaskQueue getErrorQueue()
    {
        return errorQueue;
    }

    public int getRetryCount()
    {
        return retryCount;
    }

    public void setRetryCount(int retryCount)
    {
        this.retryCount = retryCount;
    }

    public static class TaskDecorator implements Task, Serializable
    {
        private Task task;
        private int executionCount = 0;

        public TaskDecorator(Task task)
        {
            this.task = task;
        }

        public void execute() throws Exception
        {
            executionCount++;
            task.execute();
        }

        public int getExecutionCount()
        {
            return executionCount;
        }

        public Task getTask()
        {
            return task;
        }
    }
}
