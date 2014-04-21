package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.comments.DefaultCommentManager;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import org.apache.log4j.Logger;

/**
 * Upgrade task to copy all the comments create date to updated date and authors to udpateAuthors.
 */
public class UpgradeTask_Build206 extends AbstractReindexUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build206.class);
    private OfBizDelegator ofBizDelegator;

    public UpgradeTask_Build206(OfBizDelegator ofBizDelegator, ApplicationProperties applicationProperties, IssueIndexManager indexManager)
    {
        super(applicationProperties, indexManager);
        this.ofBizDelegator = ofBizDelegator;
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "This task copies values all comments authors and create dates columns to updateAuthors and updated columns respectively.";
    }

    /**
     * Returns 206 as string
     *
     * @return 206 as string
     */
    public String getBuildNumber()
    {
        return "206";
    }

    /**
     * Copies the values from author column over to updatedauthor column and
     * values from created column over to updated column for all records in
     * {@link DefaultCommentManager#COMMENT_ENTITY} table where type=comment.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode) throws Exception
    {
        LOG.info("About to begin copying comments author to updateAuthor and created date to updated date in the database.");

        ofBizDelegator.bulkCopyColumnValuesByAnd(DefaultCommentManager.COMMENT_ENTITY,
                EasyMap.build("updateauthor", "author", "updated", "created"),
                EasyMap.build("type", "comment"));
        LOG.info("Finished copying comments author to updateAuthor and created date to updated date in the database.");

        // Fire off a reindex which we need because we have updated all the comments updated date and updateAuthors.
        super.doUpgrade(setupMode);
    }
}
