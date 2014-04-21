/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.util.index.Contexts;
import org.apache.log4j.Logger;

/**
 * This task performs a reindexAll. This is due to the problems associated with:
 * http://jira.atlassian.com/browse/JRA-11861
 * <p>
 * If the re-index all is performed the log file is periodically updated with the progress.
 * <p>
 * This Upgrade Task <strong>should not</strong> be run when JIRA is being setup (i.e. installed afresh).
 */
public class UpgradeTask_Build186 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build186.class);

    private final IssueIndexManager indexManager;

    /**
     * Constructor.
     *
     * @param indexManager
     *            must not be null.
     * @throws NullPointerException
     *             if the IssueIndexManager is null
     */
    public UpgradeTask_Build186(final IssueIndexManager indexManager)
    {
        if (indexManager == null)
        {
            throw new NullPointerException(getClass().getName() + " requires an instance of " + IssueIndexManager.class.getName());
        }
        this.indexManager = indexManager;
    }

    public String getBuildNumber()
    {
        return "186";
    }

    /**
     * Should NOT fail (i.e. throw any Exceptions). If an exception occurs, the indexes are probably screwed and the
     * users will need to do a full reindexAll anyway.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        if (UpgradeTask.Status.isTaskDone("reindexAll"))
        {
            log.info("Detetected that re-index is already done, nothing to see here.");
            return;
        }

        try
        {
            if (!indexManager.isIndexingEnabled())
            {
                log.info("Indexing is disabled. Not re-indexing data.");
                return;
            }

            log.info("Re-indexing issues and comments. This make take a while. Please be patient and wait for the operation to complete.");
            indexManager.reIndexAll(Contexts.percentageLogger(indexManager, log));
            UpgradeTask.Status.setTaskDone("reindexAll");
        }
        catch (final Exception e)
        {
            log.error("Could not complete upgrade due to problems.", e);
            log.error("PLEASE REINDEX ALL DATA AFTER JIRA STARTS.");
        }
    }

    public String getShortDescription()
    {
        return "Make sure that the indexes are clean. For details see: http://jira.atlassian.com/browse/JRA-11861";
    }
}