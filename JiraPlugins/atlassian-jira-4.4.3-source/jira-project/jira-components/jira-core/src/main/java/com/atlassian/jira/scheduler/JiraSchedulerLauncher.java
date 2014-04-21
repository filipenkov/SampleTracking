package com.atlassian.jira.scheduler;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.startup.JiraLauncher;
import com.atlassian.jira.startup.JiraStartupChecklist;
import com.atlassian.jira.web.ServletContextProvider;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.scheduler.SchedulerUtil;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;

/**
 * Launches the JIRA scheduler.
 */
public class JiraSchedulerLauncher implements JiraLauncher
{
    private static final Logger log = Logger.getLogger(JiraSchedulerLauncher.class);
    private static final SchedulerUtil schedulerUtil = new SchedulerUtil();

    public void start()
    {
        try
        {
            proceedIfAllClear();
        }
        catch (RuntimeException rte)
        {
            log.fatal("A RuntimeException occurred during JiraSchedulerLauncher servlet context initialisation - " + rte.getMessage() + ".", rte);
            throw rte;
        }
        catch (Error error)
        {
            log.fatal("An Error occurred during JiraSchedulerLauncher servlet context initialisation - " + error.getMessage() + ".", error);
            throw error;
        }
    }

    private void proceedIfAllClear()
    {
        if (!JiraStartupChecklist.startupOK())
        {
            log.info("JIRA Scheduler not started: JIRA startup checklist failed.");
        }
        else if (!thereAreNoJohnsonEvents())
        {
            log.info("JIRA Scheduler not started: Johnson events detected.");
        }
        else if (!canCreateScheduler())
        {
            log.info("JIRA Scheduler not started: JIRA not setup yet.");
        }
        else
        {
            log.info("Starting the JIRA Scheduler....");
            try
            {
                schedulerUtil.initializeAndStart(getScheduler());
                log.info("JIRA Scheduler started.");
            }
            catch (SchedulerException e)
            {
                log.error("Error starting scheduler", e);
            }
        }
    }

    private boolean thereAreNoJohnsonEvents()
    {
        JohnsonEventContainer cont = JohnsonEventContainer.get(ServletContextProvider.getServletContext());
        return cont.getEvents().isEmpty();

    }

    /**
     * This ends up being called back by super.contextInitialized(servletContextEvent);
     *
     * @return true if the JIRA is setup
     */
    protected boolean canCreateScheduler()
    {
        return (ManagerFactory.getApplicationProperties().getString(APKeys.JIRA_SETUP) != null);
    }

    public void stop()
    {
        try
        {
            Scheduler scheduler = getScheduler();
            // it can be null if we stop during bootstrapping
            if (scheduler != null)
            {
                schedulerUtil.shutdownScheduler(scheduler);
            }
        }
        catch (SchedulerException e)
        {
            log.error("Error stopping scheduler", e);
        }
    }

    private Scheduler getScheduler() throws SchedulerException
    {
        return ComponentManager.getComponent(Scheduler.class);
    }
}
