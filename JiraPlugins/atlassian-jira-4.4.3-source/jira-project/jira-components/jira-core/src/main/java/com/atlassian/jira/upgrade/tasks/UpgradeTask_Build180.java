package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * Sets the comment visibility restriction to Both Role and Group restriction. This
 * task is intended only to run as a part of upgrade (not during setup) and this means
 * that only if JIRA is being upgraded will this setting be made.
 *
 * As Role comment visibility restriction is the default we want for new installations, we
 * need this upgrade task to preserve the group visibility for upgraders.
 */
public class UpgradeTask_Build180 extends AbstractUpgradeTask
{
    /**
     * Returns 180
     * @return 180
     */
    public String getBuildNumber()
    {
        return "180";
    }

    /**
     * Sets the "jira.comment.level.visibility.groups" application property to true.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        getApplicationProperties().setOption(APKeys.COMMENT_LEVEL_VISIBILITY_GROUPS, true);
    }

    /**
     * Returns a short description of this upgrade task
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "Maintains group comment visibility in addition to the new role comment visibility for upgraded instances of Jira";
    }

}
