package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.opensymphony.util.TextUtils;

/**
 * Checks and removes the jira.trackback.exclude.pattern if its NULL/empty string. This upgrade task is
 * to fix {@link com.atlassian.jira.upgrade.tasks.UpgradeTask_Build183} for database's which
 * treat empty strings as NULL (eg. Oracle).
 * see JRA-11956.
 */
public class UpgradeTask_Build187 extends AbstractUpgradeTask
{
    /**
     * Returns 187
     * @return 187
     */
    public String getBuildNumber()
    {
        return "187";
    }

    /**
     * Remove the "jira.trackback.exclude.pattern" if it is null or an empty string.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        String trackbackProperty = getApplicationProperties().getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN);
        if (!TextUtils.stringSet(trackbackProperty))
        {
            //do not store NULL or empty string "" because oracle/sybase is incapable of storing "" - JRA-11956
            getApplicationProperties().setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, null);
        }
    }

    /**
     * Returns a short description of this upgrade task
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "Remove jira.trackback.exclude.pattern application property if it is null or an empty string when upgrading JIRA.";
    }
}
