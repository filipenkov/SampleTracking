package com.atlassian.upm.rest.async;

import java.util.concurrent.CountDownLatch;

import com.atlassian.upm.rest.MediaTypes;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

public class CancellableTaskStatus extends TaskStatus
{
    private CountDownLatch latch;


    @JsonCreator
    public CancellableTaskStatus(@JsonProperty("done") boolean done)
    {
        super(done, MediaTypes.CANCELLABLE_TASK_JSON);
        latch = new CountDownLatch(1);
    }

    public void await() throws InterruptedException
    {
        latch.await();
    }

    public void cancel()
    {
        latch.countDown();
    }
}
