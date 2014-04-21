/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueLongFieldComparator;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.TimeTrackingStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

public class TimeEstimateSystemField extends AbstractDurationSystemField
{
    public TimeEstimateSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.TIME_ESTIMATE, "common.concepts.remaining.estimate", "common.concepts.remaining.estimate", ORDER_DESCENDING, new IssueLongFieldComparator(IssueFieldConstants.TIME_ESTIMATE), velocityManager, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return TimeTrackingStatisticsMapper.TIME_ESTIMATE_CURR;
    }

    public String getHiddenFieldId()
    {
        return IssueFieldConstants.TIMETRACKING;
    }

    protected Long getDuration(Issue issue)
    {
        return issue.getEstimate();
    }
}
