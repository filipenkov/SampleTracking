package com.atlassian.core.task;

/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 26/04/2006
 * Time: 14:53:47
 * To change this template use File | Settings | File Templates.
 */
public interface TaskQueueWithErrorQueue extends TaskQueue
{
    TaskQueue getErrorQueue();
}
