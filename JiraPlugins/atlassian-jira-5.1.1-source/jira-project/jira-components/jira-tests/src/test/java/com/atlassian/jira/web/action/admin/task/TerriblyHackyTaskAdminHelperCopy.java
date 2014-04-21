package com.atlassian.jira.web.action.admin.task;

import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.web.action.admin.index.IndexCommandResult;

import java.util.Random;
import java.util.concurrent.Callable;

/**
 *  The name of this class describes what it does.  Its a hack to allow tasks to turn up during testing of the
 *  TaskAdmin page.  It creates between 2 and 7 new tasks if the number of running tasks in the system is
 *  less then 3.
 *
 *  The code is left here becauses its useful but not really needed for production code.
 */
public class TerriblyHackyTaskAdminHelperCopy
{

    static void awfulHackToKickOffSomeTasksMaybe(final TaskManager taskManager)
    {
        if (taskManager.getAllTasks().size() < 3)
        {
            final int numTasks = rand(2, 7);
            for (int i = 0; i < numTasks; i++)
            {
                final TaskContext taskContext = new HackyTaskContext(i);
                final String taskDescs[] = { "Sniggling the tagwoggles", "Tassling the swog hozzles", "Florging sein Keinfemers", "Joring mein ein hamfers" };
                final String desc = taskDescs[rand(0, taskDescs.length - 1)];
                taskManager.submitTask(new HackyCallable(), desc + " - task " + i, taskContext);
            }
        }
    }

    static class HackyCallable implements Callable<IndexCommandResult>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;

        public IndexCommandResult call() throws Exception
        {
            for (int i = 0; i <= 100; i++)
            {
                final int sleepTime = rand(100, 500);
                Thread.sleep(sleepTime);

                final String taskDescs[] = { "Just woke up after a small sleep", "Listening to the cricket", "Chewing the fat", "Waiting for change", };
                final String taskSections[] = { "Working", "Farting", "Gurgling", "Sleeping", "Farting", "Smoozing", };
                final String taskType = taskSections[rand(0, taskSections.length - 1)];
                final String desc = taskDescs[rand(0, taskDescs.length - 1)];
                taskProgressSink.makeProgress(i, taskType, desc);
            }
            return new IndexCommandResult(666);
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = taskProgressSink;
        }

    }

    static class HackyTaskContext implements TaskContext
    {

        int contextId;

        public HackyTaskContext(final int contextId)
        {
            this.contextId = contextId;
        }

        public String buildProgressURL(final Long taskId)
        {
            return "/secure/admin/jira/IndexProgress.jspa?taskId=" + taskId;
        }

        @Override
        public int hashCode()
        {
            return contextId;
        }

        @Override
        public boolean equals(final Object object)
        {
            if (object instanceof HackyTaskContext)
            {
                return ((HackyTaskContext) object).contextId == contextId;
            }
            return false;
        }
    }

    static Random rn = new Random();

    static long rand(long lo, long hi)
    {

        if (lo > hi)
        {
            final long save = lo;
            lo = hi;
            hi = save;
        }
        final long range = hi - lo + 1;

        // compute a fraction of the range, 0 <= frac < range
        final long frac = (long) (range * rn.nextDouble());

        // add the fraction to the lo value and return the sum
        return (frac + lo);
    }

    /** get random numbers in a range, lo <= number <= hi */
    static int rand(final int lo, final int hi)
    {
        return (int) rand((long) lo, (long) hi);
    }

}
