/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Jun 8, 2004
 * Time: 4:52:14 PM
 */
package com.atlassian.core.logging;

import org.apache.log4j.spi.LoggingEvent;

import java.util.Date;

public class DatedLoggingEvent
{
    private final long timeInMillis;
    private final LoggingEvent event;

    public DatedLoggingEvent(long timeInMillis, LoggingEvent event)
    {
        this.timeInMillis = timeInMillis;

        this.event = event;
    }

    public LoggingEvent getEvent()
    {
        return event;
    }

    public long getTimeInMillis()
    {
        return timeInMillis;
    }

    public Date getDate()
    {
        return new Date(timeInMillis);
    }
}