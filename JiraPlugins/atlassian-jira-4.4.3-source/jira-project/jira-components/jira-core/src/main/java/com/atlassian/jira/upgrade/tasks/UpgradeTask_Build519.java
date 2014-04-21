package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * Reindex upgrade task for JRA-20241.
 *
 * @since v4.1
 */
public class UpgradeTask_Build519 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build519(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "519";
    }
}
