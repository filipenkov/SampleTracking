package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * Do a reindex of JIRA to enable voters and watchers searching.
 *
 * @since v4.1
 */
public class UpgradeTask_Build518 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build518(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "518";
    }

    @Override
    public String getShortDescription()
    {
        return "Reindexing all data in JIRA to enable voters and watchers searching.";
    }
}