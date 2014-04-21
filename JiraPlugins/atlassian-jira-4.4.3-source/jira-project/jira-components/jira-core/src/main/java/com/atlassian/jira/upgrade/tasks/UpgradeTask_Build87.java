package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IssueIndexManager;

public class UpgradeTask_Build87 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build87(ApplicationProperties applicationProperties, IssueIndexManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    public String getBuildNumber()
    {
        return "87";
    }

    public String getShortDescription()
    {
        return "Reindexing all data in JIRA to allow sorting using Lucene.";
    }

}
