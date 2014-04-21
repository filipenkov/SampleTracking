package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * Reindexes JIRA due to the introduction of the {@link com.atlassian.jira.sharing.SharedEntityColumn#IS_SHARED} field
 * for shared entities which we know use so administrators can search and manage all shared entities in JIRA.
 *
 * @since v4.4
 */
public class UpgradeTask_Build660 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build660(final ApplicationProperties applicationProperties,
            final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "660";
    }

    @Override
    public String getShortDescription()
    {
        return super.getShortDescription() + "Required for management of filters and dashboards by JIRA administrators.";
    }
}
