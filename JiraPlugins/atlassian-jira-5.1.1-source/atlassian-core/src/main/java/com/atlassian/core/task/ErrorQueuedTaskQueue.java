package com.atlassian.core.task;


public class ErrorQueuedTaskQueue extends AbstractErrorQueuedTaskQueue
{
    public ErrorQueuedTaskQueue()
    {
        super(new DefaultTaskQueue(), new LocalFifoBuffer());
    }
}
