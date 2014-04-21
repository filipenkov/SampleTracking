package com.atlassian.jira.issue.index.job;

import com.atlassian.jira.ComponentManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class OptimizeIndexJob implements Job
{
    private static final Logger log = Logger.getLogger(OptimizeIndexJob.class);

    public void execute(final JobExecutionContext context) throws JobExecutionException
    {
        try
        {
            log.info("Optimize Index Job running...");
            final long duration = ComponentManager.getInstance().getIndexLifecycleManager().optimize();
            if (duration >= 0L)
            {
                log.info("Indexes Optimized. Took: " + duration + " milliseconds.");
            }
            else
            {
                log.info("Could not optimize indexes.");
            }
        }
        catch (final Exception e)
        {
            // Quartz advises to wrap the job code in try/catch Exception block.
            log.error("Error occurred while optimizing indexes.", e);
            throw new JobExecutionException(e);
        }

        log.info("Optimize Index Job complete.");
    }
}
