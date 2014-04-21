/**
 * Copyright 2008 Atlassian Pty Ltd 
 */
package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.util.index.IndexLifecycleManager;

/**
 * @since v3.13
 */
public class UpgradeTask_Build326 extends AbstractReindexUpgradeTask
{
    public UpgradeTask_Build326(final ApplicationProperties applicationProperties, final IndexLifecycleManager indexManager)
    {
        super(applicationProperties, indexManager);
    }

    public String getBuildNumber()
    {
        return "326";
    }

    public String getShortDescription()
    {
        return "Reindexing all data in JIRA to include Favourites.";
    }
}
