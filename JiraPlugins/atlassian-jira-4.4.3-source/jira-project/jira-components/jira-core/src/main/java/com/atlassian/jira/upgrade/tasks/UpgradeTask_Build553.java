package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * Do a reindex of JIRA to ensure that index is up to date.
 *
 * @since v4.2
 */
public class UpgradeTask_Build553 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build553(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "553";
    }
}