package com.atlassian.jira.service;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.util.log.Log4jKit;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.Date;

/**
 * Runs all services due for execution at the current time.
 */
public class ServiceRunner implements Job
{
    private static final Logger log = Logger.getLogger(ServiceRunner.class);
    private static final String MOCK_USER_NAME = "ServiceRunner";

    public void execute(final JobExecutionContext jobExecutionContext) throws JobExecutionException
    {
        setLog4JInfo(MOCK_USER_NAME, "");

        final long currentTime = System.currentTimeMillis();
        if (log.isDebugEnabled())
        {
            String time;
            synchronized (this)
            {
                time = DateUtils.ISO8601DateFormat.format(new Date(currentTime));
            }
            log.debug("ServiceRunner.runServices() Running at [" + time + "]");
        }

        for (final JiraServiceContainer service : ComponentManager.getComponent(ServiceManager.class).getServicesForExecution(currentTime))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Running Service [" + service + "]");
            }
            try
            {
                // make the logs come out with the name of the running service
                setLog4JInfo(MOCK_USER_NAME, service.getName());

                service.run();
            }
            catch (final RuntimeException e)
            {
                log.error("An error occured while trying to run service '" + service.getName() + "'. " + e.getMessage(), e);
                throw new JobExecutionException(e, true);
            }
            finally
            {
                service.setLastRun();
                setLog4JInfo(MOCK_USER_NAME, "");
            }
            if (log.isDebugEnabled())
            {
                log.debug("Finished Running Service [" + service + "]");
            }
        }
    }

    /**
     * The ServiceRunner can run inside a different thread each time.  But the Log4J MDC ins an inheritable thread local
     * and hence will inherit the details fo the web request that started the scheduler.  So we overrride it here
     * explicitly. Ideally we would do this in the quartz scheduler thread itself however we cant control this easily.
     *
     * @param userName    the user name to use
     * @param serviceName the running service name
     */
    private void setLog4JInfo(final String userName, final String serviceName)
    {
        Log4jKit.clearMDC();
        Log4jKit.putToMDC(userName, "", "", serviceName, "");
    }
}
