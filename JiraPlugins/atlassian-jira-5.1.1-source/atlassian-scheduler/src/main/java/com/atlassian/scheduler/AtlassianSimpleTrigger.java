package com.atlassian.scheduler;

import org.quartz.SimpleTrigger;
import org.quartz.Calendar;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Owen
 * Date: 12/09/2003
 * Time: 17:49:58
 * To change this template use Options | File Templates.
 */
public class AtlassianSimpleTrigger extends SimpleTrigger
{
    public AtlassianSimpleTrigger()
    {
        super();    //To change body of overriden methods use Options | File Templates.
    }

    public AtlassianSimpleTrigger(String string, String string1)
    {
        super(string, string1);    //To change body of overriden methods use Options | File Templates.
    }

    public AtlassianSimpleTrigger(String string, String string1, int i, long l)
    {
        super(string, string1, i, l);    //To change body of overriden methods use Options | File Templates.
    }

    public AtlassianSimpleTrigger(String string, String string1, Date date)
    {
        super(string, string1, date);    //To change body of overriden methods use Options | File Templates.
    }

    public AtlassianSimpleTrigger(String string, String string1, Date date, Date date1, int i, long l)
    {
        super(string, string1, date, date1, i, l);    //To change body of overriden methods use Options | File Templates.
    }

    public AtlassianSimpleTrigger(String string, String string1, String string2, String string3, Date date, Date date1, int i, long l)
    {
        super(string, string1, string2, string3, date, date1, i, l);    //To change body of overriden methods use Options | File Templates.
    }

    public Date computeFirstFireTime(Calendar calendar)
    {
        if (getNextFireTime() == null)
        {
            setNextFireTime(new Date(System.currentTimeMillis()));
        }
        else
        {
            setNextFireTime(getFireTimeAfter(getNextFireTime()));
        }
        return getNextFireTime();
    }
}
