package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IssueIndexManager;
import org.apache.log4j.Logger;

/**
 *
 */
public class UpgradeTask_Build281 extends AbstractReindexUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build281.class);

    public UpgradeTask_Build281(ApplicationProperties applicationProperties, IssueIndexManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    public String getBuildNumber()
    {
        return "281";
    }

    public String getShortDescription()
    {
        return "Reindexing all data to for change in the way we store dates in Lucene.";
    }
}
