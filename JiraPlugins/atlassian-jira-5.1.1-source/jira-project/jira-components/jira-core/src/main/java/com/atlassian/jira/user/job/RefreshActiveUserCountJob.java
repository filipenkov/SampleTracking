package com.atlassian.jira.user.job;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * This job is responsible for flushing the active user count.  This is useful if JIRA is running with external
 * user management, where JIRA can't detect if group membership changes via actions taken in JIRA.  The results
 * of the active user count may be used to lock down JIRA if the user limit enforced by the license has been exceeded.
 *
 * @since 3.13
 */
public class RefreshActiveUserCountJob implements Job
{
    private static final Logger log = Logger.getLogger(RefreshActiveUserCountJob.class);

    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        try
        {
            if (requiresUserLimit())
            {
                refreshActiveUserCount();
            }
        }
        catch (final Exception e)
        {
            // Quartz advises to wrap the job code in try/catch Exception block.
            log.error("Error occurred while refreshing the active user count.", e);
            throw new JobExecutionException(e);
        }
    }

    private void refreshActiveUserCount()
    {
        final String timerString = "Refreshing active user count in background thread.";
        try
        {
            log.debug("Started " + timerString);
            UtilTimerStack.push(timerString);
            final UserUtil userUtil = getUserUtil();
            //first clear the user count
            userUtil.clearActiveUserCount();
            //then re-calculate the user count straight away to ensure this is done in a background thread.
            userUtil.getActiveUserCount();
        }
        finally
        {
            UtilTimerStack.pop(timerString);
            log.debug("Finished " + timerString);
        }
    }

    boolean requiresUserLimit()
    {
        final JiraLicenseService jiraLicenseService = ComponentManager.getComponentInstanceOfType(JiraLicenseService.class);
        return !jiraLicenseService.getLicense().isUnlimitedNumberOfUsers();
    }

    UserUtil getUserUtil()
    {
        return ComponentAccessor.getUserUtil();
    }
}
