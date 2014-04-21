package com.atlassian.jira.issue.fields;

import com.atlassian.core.ofbiz.comparators.OFBizDateComparator;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.CreatedDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class CreatedSystemField extends NavigableFieldImpl implements SearchableField, DateField
{
    private final SearchHandlerFactory searchHandlerFactory;
    private final ColumnViewDateTimeHelper columnViewDateTimeHelper;

    public CreatedSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, CreatedDateSearchHandlerFactory searchHandlerFactory, ColumnViewDateTimeHelper columnViewDateTimeHelper)
    {
        super(IssueFieldConstants.CREATED, "issue.field.created", "issue.column.heading.created", ORDER_DESCENDING, new OFBizDateComparator(IssueFieldConstants.CREATED), velocityManager, applicationProperties, authenticationContext);
        this.searchHandlerFactory = searchHandlerFactory;
        this.columnViewDateTimeHelper = columnViewDateTimeHelper;
    }

    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_CREATED_STATSMAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        return columnViewDateTimeHelper.render(this, fieldLayoutItem, displayParams, issue, issue.getCreated());
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }
}
