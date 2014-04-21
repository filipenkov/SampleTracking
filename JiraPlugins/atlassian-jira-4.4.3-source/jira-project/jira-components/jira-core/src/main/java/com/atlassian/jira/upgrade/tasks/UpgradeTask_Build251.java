/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.index.IssueIndexManager;

/**
 * This upgrade task will case a re-index, which allows for the new {@link com.atlassian.jira.issue.index.indexers.impl.IssueKeyIndexer}
 * to index issue key and issue key number part.
 */
public class UpgradeTask_Build251 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build251(ApplicationProperties applicationProperties, IssueIndexManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    public String getBuildNumber()
    {
        return "251";
    }

    public String getShortDescription()
    {
        return "Reindexing all data to index issue key number part.";
    }
}
