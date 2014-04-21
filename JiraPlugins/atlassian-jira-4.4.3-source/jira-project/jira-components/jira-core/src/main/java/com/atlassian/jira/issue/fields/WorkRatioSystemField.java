/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.WorkRatioComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.WorkRatioSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.LongFieldStatisticsMapper;
import com.atlassian.jira.issue.worklog.WorkRatio;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

public class WorkRatioSystemField extends NavigableFieldImpl implements SearchableField
{
    private final SearchHandlerFactory searcherHandlerFactory;

    public WorkRatioSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, WorkRatioSearchHandlerFactory handlerFactory)
    {
        super(IssueFieldConstants.WORKRATIO, "issue.field.workratio", "issue.column.heading.workratio", ORDER_ASCENDING, new WorkRatioComparator(), velocityManager, applicationProperties, authenticationContext);
        this.searcherHandlerFactory = handlerFactory;
    }

    public LuceneFieldSorter getSorter()
    {
        return LongFieldStatisticsMapper.WORK_RATIO;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        if (isWorkEstimateExists(issue))
        {
            velocityParams.put(getId(), new Long(getWorkRatio(issue)));
        }
        return renderTemplate("workratio-columnview.vm", velocityParams);
    }

    private boolean isWorkEstimateExists(Issue issue)
    {
        return issue.getOriginalEstimate() != null;
    }

    public long getWorkRatio(Issue issue)
    {
        return WorkRatio.getWorkRatio(issue);
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searcherHandlerFactory.createHandler(this);
    }
}
