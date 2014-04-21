/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.JiraDurationUtils;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class UpgradeTask_Build48 extends AbstractUpgradeTask
{
    public String getBuildNumber()
    {
        return "48";
    }

    public String getShortDescription()
    {
        return "Creates timeoriginalestimate field in Issue table. Add default hoursPerDay and daysPerWeek properties";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        ManagerFactory.getApplicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR, false);
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_FORMAT, JiraDurationUtils.FORMAT_PRETTY);
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_DEFAULT_UNIT, DateUtils.Duration.MINUTE.toString().toUpperCase());
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "5");
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "8");
        ManagerFactory.getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK, "This value can not be changed after work has begun on the issue.");

        List l = CoreFactory.getGenericDelegator().findAll("Issue");

        for (int i = 0; i < l.size(); i++)
        {
            GenericValue issue = (GenericValue) l.get(i);
            Long timeOriginalEstimate = null;
            if (issue.getLong("timeestimate") != null)
            {
                if (issue.getLong("timespent") == null)
                {
                    timeOriginalEstimate = issue.getLong("timeestimate");
                }
                else
                {
                    timeOriginalEstimate = new Long(issue.getLong("timeestimate").longValue() + issue.getLong("timespent").longValue());
                }
            }
            issue.set("timeoriginalestimate", timeOriginalEstimate);
            issue.store();
        }
    }
}
