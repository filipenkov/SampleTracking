package com.atlassian.jira.util.system;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.upgrade.UpgradeLauncher;
import com.atlassian.scheduler.SchedulerUtil;
import org.apache.log4j.Logger;
import org.quartz.Scheduler;

import javax.servlet.ServletContext;

/**
 * Since this class causes PICO to commit suicide, it is very careful to be completely stateless and not keep references
 * to any things inside PICO itself.
 *
 * @since v4.0
 */
public class JiraSystemRestarterImpl implements JiraSystemRestarter
{
    private static final Logger log = Logger.getLogger(JiraSystemRestarterImpl.class);
    private static final String JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED = "JIRA is about to be internally restarted";
    private static final String JIRA_HAS_BEEN_INTERNALLY_RESTARTED = "JIRA has been internally restarted";

    public void ariseSirJIRA()
    {
        log.info(JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED);

        stopServicesInOtherThread();

        restartPico();

        startServicesInOtherThreads();

        log.info(JIRA_HAS_BEEN_INTERNALLY_RESTARTED);
    }

    public void ariseSirJIRAandUpgradeThySelf(ServletContext servletContext)
    {
        log.info(JIRA_IS_ABOUT_TO_BE_INTERNALLY_RESTARTED);

        stopServicesInOtherThread();

        restartPico();

        UpgradeLauncher.checkIfUpgradeNeeded(servletContext);

        startServicesInOtherThreads();

        log.info(JIRA_HAS_BEEN_INTERNALLY_RESTARTED);
    }

    private void restartPico()
    {
        // Reinitialise all the manager objects
        log.info("Restarting JIRA code components...");
        ManagerFactory.globalRefresh();
        log.info("JIRA code components started");
    }

    private void stopServicesInOtherThread()
    {
        // Stop the scheduler
        log.info("Stopping the JIRA Scheduler...");
        final SchedulerUtil schedUtil = new SchedulerUtil();
        final Scheduler sched = ManagerFactory.getScheduler();
        schedUtil.shutdownScheduler(sched);
        log.info("JIRA Scheduler Stopped");

        log.info("Empting the JIRA Mail Queue...");
        try
        {
            ManagerFactory.getMailQueue().sendBuffer();
            log.info("JIRA Mail Queue emptied");
        }
        catch (final Exception e)
        {
            log.warn("Failed to empty the Mail Queue: " + e.getMessage(), e);
        }
    }

    private void startServicesInOtherThreads()
    {
        // initialize the new scheduler that was created by pico
        log.info("Restarting the JIRA Scheduler...");
        final SchedulerUtil schedUtil = new SchedulerUtil();
        schedUtil.initializeAndStart(ManagerFactory.getScheduler());
        log.info("JIRA Scheduler started");
    }
}
