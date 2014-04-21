/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.DateUtils;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.util.JiraDurationUtils;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;

public class UpgradeTask_Build48 extends AbstractUpgradeTask
{
    public UpgradeTask_Build48()
    {
        super(false);
    }

    public String getBuildNumber()
    {
        return "48";
    }

    public String getShortDescription()
    {
        return "Creates timeoriginalestimate field in Issue table. Add default hoursPerDay and daysPerWeek properties";
    }

    public void doUpgrade(boolean setupMode) throws GenericEntityException
    {
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING, true);
        ComponentAccessor.getApplicationProperties().setOption(APKeys.JIRA_OPTION_TIMETRACKING_ESTIMATES_LEGACY_BEHAVIOUR, false);
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_FORMAT, JiraDurationUtils.FORMAT_PRETTY);
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_DEFAULT_UNIT, DateUtils.Duration.MINUTE.toString().toUpperCase());
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_DAYS_PER_WEEK, "5");
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_TIMETRACKING_HOURS_PER_DAY, "8");
        ComponentAccessor.getApplicationProperties().setString(APKeys.JIRA_ISSUE_DESC_ORIGINAL_TIMETRACK, "This value can not be changed after work has begun on the issue.");

        List l = getOfBizDelegator().findAll("Issue");

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
