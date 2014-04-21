package com.atlassian.upm.rest.async;

import java.net.URI;

import static com.atlassian.upm.rest.async.AsynchronousTask.Type.CANCELLABLE;

public class CancellableAsynchronousTask extends AsynchronousTask<CancellableTaskStatus>
{
    public CancellableAsynchronousTask(String username)
    {
        super(CANCELLABLE, username);
    }

    public void accept()
    {
        status = startCancellableStatus();
    }

    private static CancellableTaskStatus startCancellableStatus()
    {
        return new CancellableTaskStatus(false);
    }

    private static CancellableTaskStatus finishCancellableStatus()
    {
        return new CancellableTaskStatus(true);
    }

    public URI call() throws Exception
    {
        status.await();
        status = finishCancellableStatus();
        return null;
    }
}
