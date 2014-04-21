package com.atlassian.core.logging;

import org.apache.log4j.spi.LoggingEvent;

import java.util.List;
import java.util.LinkedList;

/**
 * A simple ThreadLocal stack to prevent circular content includes.
 */
public class ThreadLocalErrorCollection
{
    public static final int DEFAULT_LIMIT =100;
    private static ThreadLocal threadLocalCollection = new ThreadLocal(){
        protected Object initialValue(){
            return new LinkedList();
        }
    };

    private static ThreadLocal threadLocalEnabled = new ThreadLocal(){
        protected Object initialValue(){
            return Boolean.FALSE;
        }
    };

    private static int limit = DEFAULT_LIMIT;

    public static void add(long timeInMillis, LoggingEvent e)
    {
        if (!isEnabled())
            return;

        List loggingEvents = getList();

        loggingEvents.add(new DatedLoggingEvent(timeInMillis, e));

        while (loggingEvents.size() > limit)
            loggingEvents.remove(0);

    }

    public static void clear()
    {
        getList().clear();
    }

    public static List getList()
    {
        List list = (List) threadLocalCollection.get();
        return list;
    }

    public static boolean isEmpty()
    {
        return getList().isEmpty();
    }

    public static int getLimit()
    {
        return limit;
    }

    public static void setLimit(int limit)
    {
        ThreadLocalErrorCollection.limit = limit;
    }

    public static boolean isEnabled()
    {
        Boolean enabledState = (Boolean) threadLocalEnabled.get();
        return Boolean.TRUE.equals(enabledState);
    }

    public static void enable()
    {
        threadLocalEnabled.set(Boolean.TRUE);
    }

    public static void disable()
    {
        threadLocalEnabled.set(Boolean.FALSE);
    }
}
