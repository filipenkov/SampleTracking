/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;

public class UpgradeTask_Build10 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build10.class);

    public UpgradeTask_Build10()
    {
        super(false);
    }

    public String getBuildNumber()
    {
        return "10";
    }

    /**
     * This is mainly just to test the new upgrade system based on build numbers.
     *
     * It will turn issue caching on, and issue linking off (both would be off by default).
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        log.debug("UpgradeTask_Build10 - setting up defaults for new features");

        //JRA-8060 - Issue caching should not be turned on by default!
        //getApplicationProperties().setOption(APKeys.JIRA_OPTION_CACHE_ISSUES, true);
        getApplicationProperties().setOption(APKeys.JIRA_OPTION_ISSUELINKING, false);

        //if you change these - you want to change them in setupcomplete.java also
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ENVIRONMENT, "For example operating system, software platform and/or hardware specifications (include as appropriate for the issue).");
        getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_TIMETRACK, "An estimate of how much work remains until this issue will be resolved.<br>\n" + "The format of this is ' *w *d *h *m ' (representing weeks, days, hours and minutes - where * can be any number)<br>\n" + "Examples: 4d, 5h 30m, 60m and 3w.");
    }
}
