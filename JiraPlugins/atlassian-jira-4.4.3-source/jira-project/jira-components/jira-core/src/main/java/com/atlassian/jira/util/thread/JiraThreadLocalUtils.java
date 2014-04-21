package com.atlassian.jira.util.thread;

import com.atlassian.jira.security.JiraAuthenticationContextImpl;
import com.atlassian.jira.util.ImportUtils;
import com.atlassian.jira.util.searchers.ThreadLocalSearcherCache;
import com.atlassian.jira.web.filters.ThreadLocalQueryProfiler;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.TransactionUtil;

import java.io.IOException;

/**
 * This class has static methods that perform a number of standard operations at the start and end of "runnable code"
 * such as a {@link com.atlassian.jira.service.JiraServiceContainerImpl} or a {@link com.atlassian.jira.task.TaskManagerImpl}.
 * <p/>
 * The main purpose of this class is to setup and clear ThreadLocal variables that can otherwise interfere with the
 * smooth running of JIRA.
 * <p/>
 * You MUST remember to call postCall() in a finally{} block like this :
 * <p/>
 * <pre>
 * run() {
 *      JiraThreadLocalUtils.preCall();
 *      try {
 *          // do runnable code here
 *      } finally {
 *          JiraThreadLocalUtils.postCall(log, myProblemDeterminationCallback);
 *      }
 *  }
 * <pre>
 *
 * @since v3.13
 */
public class JiraThreadLocalUtils
{
    /**
     * This should be called "before" an "runnable code" is called.  This will setup a clean ThreadLocal environment
     * for the runnable code to execute in.
     */
    public static void preCall()
    {
        JiraAuthenticationContextImpl.clearRequestCache();
        ThreadLocalQueryProfiler.start();
    }

    /**
     * This should be called in a finally {} block to clear up ThreadLocals once the runnable stuff has been done.
     *
     * @param log                          the log to write error messages to in casse of any problems
     * @param problemDeterminationCallback the callback to invoke in case where problems are detected after the runnable code is done running and its not cleaned up properly.  This can be null.
     */
    public static void postCall(final Logger log, final ProblemDeterminationCallback problemDeterminationCallback)
    {
        try
        {
            ThreadLocalQueryProfiler.end();
        }
        catch (final IOException e)
        {
            log.error("Unable to call ThreadLocalQueryProfiler.end()", e);
        }

        ThreadLocalSearcherCache.resetSearchers();

        if (!ImportUtils.isIndexIssues())
        {
            log.error("Indexing thread local not cleared. Clearing...");
            ImportUtils.setIndexIssues(true);
        }

        try
        {
            if (TransactionUtil.getLocalTransactionConnection() != null)
            {
                try
                {
                    if (problemDeterminationCallback != null)
                    {
                        problemDeterminationCallback.onOpenTransaction();
                    }
                }
                finally
                {
                    // Close the connection and clear the thead local
                    TransactionUtil.closeAndClearThreadLocalConnection();
                }
            }
        }
        catch (final Throwable t)
        {
            log.error("Error while inspecting transaction thread local.", t);
        }
    }

    /**
     * This interface is used as a callback mechansim in the case where "runnnable code" has completed and left
     * things in a bad way.  Typically all that can be done is to log the problem.  This interface allows
     * the detection of the problem and the logging to be separated.
     */
    public interface ProblemDeterminationCallback
    {

        /**
         * Called when the system detects an open database transaction still in play.  This should only be invoked
         * if the called code "stuffed" up and forgot to clean up after itself.  Its not expected that this should
         * ever be called if things are dont properly..
         */
        public void onOpenTransaction();
    }
}
