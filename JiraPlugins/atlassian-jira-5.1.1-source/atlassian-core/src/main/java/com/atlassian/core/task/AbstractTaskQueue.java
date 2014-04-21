package com.atlassian.core.task;

import org.apache.log4j.Logger;

import java.sql.Timestamp;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 26/04/2006
 * Time: 11:17:36
 * To change this template use File | Settings | File Templates.
 */
public abstract class AbstractTaskQueue implements TaskQueue
{
    private static final transient Logger log = Logger.getLogger(AbstractTaskQueue.class);
    protected FifoBuffer buffer;

    private boolean flushing;
    private Timestamp flushStarted;


    public AbstractTaskQueue(FifoBuffer buffer)
    {
        this.buffer = buffer;
    }

    public void flush()
    {
        if (flushing)
            return;

        flushing = true;
        flushStarted = new Timestamp(System.currentTimeMillis());

        try
        {
            Task task;
            while ((task = (Task) buffer.remove()) != null)
            {
                log.debug("Executing: " + task);
                try
                {
                    task.execute();
                }
                catch (Exception e)
                {
                    handleException(task, e);
                }
            }
        }
        finally
        {
            flushing = false;
            flushStarted = null;
        }
    }

    protected void handleException(Task task, Exception e)
    {
        log.error("Failed to execute task : " + task, e);
    }

    public int size()
    {
        return buffer.size();
    }

    public void addTask(Task task)
    {
        log.debug("Queued: " + task);
        buffer.add(task);
    }

    /**
     * @deprecated use getTasks() instead.
     */
    public Collection getQueue()
    {
        return buffer.getItems();
    }

    public boolean isFlushing()
    {
        return flushing;
    }

    public Timestamp getFlushStarted()
    {
        return flushStarted;
    }

    public void clear()
    {
        buffer.clear();
    }

    public Collection getTasks()
    {
        return buffer.getItems();
    }
}

