package com.atlassian.core.task;

import java.io.Serializable;

/**
 * An Arbitary task used to execute some code. Tasks can be queued up on a queue and executed when the
 * queue is flushed
 * @see TaskQueue
 *
 * @author Ross Mason
 */
public interface Task extends Serializable
{
   /**
    * The execute method is used to invoke the task.
    *
    * @throws Exception if the task fails to execute
    */
    public void execute() throws Exception;
}
