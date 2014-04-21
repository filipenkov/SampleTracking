/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jun 8, 2004
 * Time: 4:31:53 PM
 */
package com.atlassian.core.logging;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

public class ThreadLocalErrorLogAppender extends AppenderSkeleton
{
    public ThreadLocalErrorLogAppender()
    {
    }

    protected void append(LoggingEvent event)
    {
        ThreadLocalErrorCollection.add(System.currentTimeMillis(), event);
    }

    public void close()
    {
    }

    public boolean requiresLayout()
    {
        return false;
    }
}