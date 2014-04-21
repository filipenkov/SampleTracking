/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: Dec 5, 2002
 * Time: 5:36:03 PM
 * To change this template use Options | File Templates.
 */
package com.atlassian.scheduler;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Category;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;

public class SchedulerLauncher implements ServletContextListener
{
    private static final Category log = Category.getInstance(SchedulerLauncher.class);
    private static final SchedulerUtil schedulerUtil = new SchedulerUtil();

    public void contextInitialized(ServletContextEvent event)
    {
        log.debug("Initializing Scheduler Launcher.");
        if (canCreateScheduler())
        {
            try
            {
                schedulerUtil.initializeAndStart(getScheduler());
            }
            catch (Exception e)
            {
                log.error(e, e);
            }
        }
        else
        {
            log.info("The scheduler has not been launched at this time.");
        }
    }

    public void contextDestroyed(ServletContextEvent event)
    {
        log.debug("Destroying Scheduler Launcher.");
        try
        {
            schedulerUtil.shutdownScheduler(getScheduler());
        }
        catch (Exception e)
        {
            log.error(e, e);
        }
    }

    public Scheduler getScheduler() throws SchedulerException
    {
        return new StdSchedulerFactory().getScheduler();
    }

    protected boolean canCreateScheduler()
    {
        return true;
    }
}
