/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.statistics.IssueKeyStatisticsMapper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

public class KeySystemField extends NavigableFieldImpl
{

    public KeySystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext)
    {
        super(IssueFieldConstants.ISSUE_KEY, "issue.field.issuekey", "issue.column.heading.issuekey", ORDER_ASCENDING, new IssueKeyComparator(), velocityManager, applicationProperties, authenticationContext);
    }

    public LuceneFieldSorter getSorter()
    {
        return IssueKeyStatisticsMapper.MAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        Map velocityParams = getVelocityParams(fieldLayoutItem, getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("applicationProperties", getApplicationProperties());
        return renderTemplate("key-columnview.vm", velocityParams);
    }
}
