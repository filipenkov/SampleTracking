package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * This ended up a No-Op because its not the final final upgrade task needed for re-index
 *
 * @since v4.4
 */
public class UpgradeTask_Build644 extends AbstractUpgradeTask
{
    public UpgradeTask_Build644()
    {
        super(false);
    }

    @Override
    public String getBuildNumber()
    {
        return "644";
    }

    @Override
    public String getShortDescription()
    {
        return "No-op upgrade tasks that led a an all too breif existence but we are all the better for knowing it";
    }

    @Override
    public void doUpgrade(boolean setupMode) throws Exception
    {
    }
}
