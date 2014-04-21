package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * @since v4.4
 */
public class UpgradeTask_Build646 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build646(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    @Override
    public String getBuildNumber()
    {
        return "646";
    }

    @Override
    public String getShortDescription()
    {
        return "Reindexing all data in JIRA, because of the new index for the DatePicker custom field and the DueDate system field, and the Search Change History fields.";
    }
}
