package com.atlassian.streams.spi;

import com.atlassian.streams.api.StreamsException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Indicates that a feed query was cancelled.
 */
public class CancelledException extends StreamsException
{
    private static final Logger log = LoggerFactory.getLogger(CancelledException.class);

    public CancelledException()
    {
        super();
    }

    public CancelledException(String s)
    {
        super(s);
    }

    public CancelledException(String s, InterruptedException cause)
    {
        super(s, cause);
    }

    public CancelledException(InterruptedException cause)
    {
        super(cause);
    }

    /**
     * Throws a CancelledException if the current thread's interrupt state is set.
     * This also clears the current thread's interrupt state.
     * <p>
     * See {@link StreamsActivityProvider#getActivityFeed(com.atlassian.streams.api.ActivityRequest)}
     * for more about the circumstances in which interrupt checking is needed.
     */
    public static void throwIfInterrupted() throws CancelledException
    {
        if (Thread.interrupted())
        {
            CancelledException e = new CancelledException();
            if (log.isDebugEnabled())
            {
                log.debug("detected thread interrupt", e);
            }
            throw e;
        }
    }
}
