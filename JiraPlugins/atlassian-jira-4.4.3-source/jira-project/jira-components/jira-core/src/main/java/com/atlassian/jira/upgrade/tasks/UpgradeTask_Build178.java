/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.upgrade.UpgradeTask;
import com.atlassian.jira.util.index.Contexts;
import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;

import java.io.IOException;

/**
 * Makes sure that the comment index has the {@link com.atlassian.jira.issue.index.DocumentConstants#COMMENT_CREATED
 * comment created date field} indexed so it can be used for comment ordering and RSS feeds.
 * <p/>
 * Note: This kicks off {@link IssueIndexManager#reIndexAll(com.atlassian.johnson.event.Event)}, so it is very careful
 * to only do so if the Comment created date is not there already. For example, if JIRA is being upgraded by importing
 * data from XML, then by the time this upgrade task is run, re-index all has already been performed. We do not want to
 * re-index again, to save time. On the other hand, if the upgrade is performed by pointing to an existing JIRA database
 * that contains data, we need to re-index.
 * <p/>
 * This task sample up to 100 Comment {@link Document Lucene Documents} from the index, and if at least one does not
 * have the Comment created date indexed, the re-index all is performed.
 * <p/>
 * If the re-index all is performed the log file is periodically updated with the progress.
 * <p/>
 * This Upgrade Task <strong>should not</strong> be run when JIRA is being setup (i.e. installed afresh).
 */
public class UpgradeTask_Build178 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build178.class);

    private static final int DEFAULT_NUMBER_OF_DOCS_TO_CHECK = 100;

    private final IssueIndexManager indexManager;

    /**
     * Constructor.
     *
     * @param indexManager must not be null.
     * @throws NullPointerException if the IssueIndexManager is null
     */
    public UpgradeTask_Build178(final IssueIndexManager indexManager)
    {
        if (indexManager == null)
        {
            throw new NullPointerException(getClass().getName() + " requires an instance of " + IssueIndexManager.class.getName());
        }
        this.indexManager = indexManager;
    }

    public String getBuildNumber()
    {
        return "178";
    }

    /**
     * Should NOT fail (i.e. throw any Exceptions). If an exception occurs, the indexes are probably screwed and the
     * users will need to do a full reindexAll anyway.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        try
        {
            if (!indexManager.isIndexingEnabled())
            {
                log.info("Indexing is disabled. Not re-indexing data.");
                return;
            }

            final IndexSearcher commentSearcher = indexManager.getCommentSearcher();
            boolean upgraded = false;
            try
            {
                final IndexReader reader = commentSearcher.getIndexReader();
                int docsToRead = Math.min(DEFAULT_NUMBER_OF_DOCS_TO_CHECK, reader.maxDoc());

                // named loop so inner loop can exit
                main : for (int i = 0; i < docsToRead; i++)
                {
                    Document commentDocument = null;

                    while (commentDocument == null)
                    {
                        try
                        {
                            commentDocument = reader.document(i);
                        }
                        catch (final IllegalArgumentException mustHaveBeenDeletedMoveOnNothingToSeeHere)
                        {
                            // check the next one
                            i++;

                            // need to look at more records but don't go past maxDocs
                            docsToRead = Math.min(docsToRead + 1, reader.maxDoc());
                            if (i >= docsToRead)
                            {
                                // we're done, break from outer loop
                                break main;
                            }
                        }
                    }
                    upgraded = (commentDocument.getField(DocumentConstants.COMMENT_CREATED) != null);
                    if (!upgraded)
                    {
                        break;
                    }
                }
            }
            finally
            {
                commentSearcher.close();
            }

            if (upgraded)
            {
                log.info("Detected that data has already been re-indexed. Not performing another re-index.");
                return;
            }

            log.info("Detetected that re-index is required.");
            log.info("Re-indexing issues and comments. This make take a while. Please be patient and wait for the operation to complete.");
            indexManager.reIndexAll(Contexts.percentageLogger(indexManager, log));
            UpgradeTask.Status.setTaskDone("reindexAll");
        }
        catch (final RuntimeException e)
        {
            handle(e);
        }
        catch (final IndexException e)
        {
            handle(e);
        }
        catch (final IOException e)
        {
            handle(e);
        }
    }

    private void handle(final Exception e)
    {
        log.error("Could not complete upgrade due to problems.", e);
        log.error("PLEASE REINDEX ALL DATA AFTER JIRA STARTS.");
    }

    public String getShortDescription()
    {
        return "Make sure that the comment index has the Comment Created Date indexed so it can be used for comment ordering";
    }
}