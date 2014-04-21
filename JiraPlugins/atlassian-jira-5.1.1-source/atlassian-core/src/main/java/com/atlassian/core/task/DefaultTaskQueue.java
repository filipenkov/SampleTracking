package com.atlassian.core.task;

/**
 * The default implementation of <code>TaskQueue</code>.
 * @see TaskQueue
 *
 * @author Ross Mason
 */

public class DefaultTaskQueue extends AbstractTaskQueue
{
    public DefaultTaskQueue()
    {
        super(new LocalFifoBuffer());
    }
}