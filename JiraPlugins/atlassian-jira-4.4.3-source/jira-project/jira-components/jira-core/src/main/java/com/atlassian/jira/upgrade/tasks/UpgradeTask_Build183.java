package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;

/**
 * This upgrade task is obsolete and replaced by {@link com.atlassian.jira.upgrade.tasks.UpgradeTask_Build187}.
 * See JRA-11956 for more details.
 * <p>
 * The {@link com.atlassian.jira.upgrade.UpgradeTask#doUpgrade(boolean)} no longer does anything.
 * <p>
 * Previously it:
 * Checks if the jira.trackback.exclude.pattern is Null. If it is, it is set to empty
 * string.  This prevents problems with velocity template generation. (When constructing a
 * velocity context, the Velocity Engine has problems reading the properties if there's a null
 * property). Also see JRA-11695 and JRA-8478.
 */
public class UpgradeTask_Build183 extends AbstractUpgradeTask
{
    /**
     * Returns 183
     * @return 183
     */
    public String getBuildNumber()
    {
        return "183";
    }

    /**
     * This upgrade task no longer does anything as it's superceded by {@link com.atlassian.jira.upgrade.tasks.UpgradeTask_Build187}.
     * Sets the "jira.trackback.exclude.pattern" to empty string if it is null.
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        /*
        String trackbackProperty = getApplicationProperties().getString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN);
        if(trackbackProperty == null)
        {
            getApplicationProperties().setString(APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN, "");
        }
        */
    }

    /**
     * Returns a short description of this upgrade task
     * @return a short description string
     */
    public String getShortDescription()
    {
        //return "Sets jira.trackback.exclude.pattern application property to an empty string if it is null when upgrading JIRA.";
        return "This is an obsolete upgrade task that does nothing, superceded by upgrade task 187";
    }

}
