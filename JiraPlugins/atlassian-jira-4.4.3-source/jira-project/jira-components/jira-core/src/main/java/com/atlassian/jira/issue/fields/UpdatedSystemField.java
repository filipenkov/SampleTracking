package com.atlassian.jira.issue.fields;

import com.atlassian.core.ofbiz.comparators.OFBizDateComparator;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.UpdatedDateSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class UpdatedSystemField extends NavigableFieldImpl implements SearchableField, DateField
{
    private final ColumnViewDateTimeHelper columnViewDateTimeHelper;
    private final SearchHandlerFactory searchHandlerFactory;

    public UpdatedSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, UpdatedDateSearchHandlerFactory searchHandlerFactory, ColumnViewDateTimeHelper columnViewDateTimeHelper)
    {
        super(IssueFieldConstants.UPDATED, "issue.field.updated", "issue.column.heading.updated", ORDER_DESCENDING, new OFBizDateComparator(IssueFieldConstants.UPDATED), velocityManager, applicationProperties, authenticationContext);
        this.columnViewDateTimeHelper = columnViewDateTimeHelper;
        this.searchHandlerFactory = notNull("searchHandlerFactory", searchHandlerFactory);
    }

    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_UPDATED_STATSMAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        return columnViewDateTimeHelper.render(this, fieldLayoutItem, displayParams, issue, issue.getUpdated());
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }
}
