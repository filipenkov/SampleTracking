package com.atlassian.crowd.integration.rest.service.util;

import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.log4j.Logger;

/**
 * This class behaves exactly like {@link MultiThreadedHttpConnectionManager},
 * with the exception of ignoring all {@link #shutdown()} calls.
 *
 * It can be used in cases when another component that must be supported makes calls to
 * {@link org.apache.commons.httpclient.MultiThreadedHttpConnectionManager#shutdownAll()}.
 */
public class ShutdownIgnoringMultiThreadedHttpConnectionManager extends MultiThreadedHttpConnectionManager
{
    private static final Logger LOG = Logger.getLogger(ShutdownIgnoringMultiThreadedHttpConnectionManager.class);

    /**
     * Logs the call and returns without shutting down.
     */
    @Override
    public void shutdown()
    {
        // Log warning with stack trace and continue.
        try
        {
            throw new IllegalStateException();
        }
        catch (IllegalStateException e)
        {
            LOG.warn("Unwanted shutdown call detected and ignored", e);
        }
    }

    /**
     * Shuts down the connection manager.
     */
    public void reallyShutdown()
    {
        super.shutdown();
    }
}
