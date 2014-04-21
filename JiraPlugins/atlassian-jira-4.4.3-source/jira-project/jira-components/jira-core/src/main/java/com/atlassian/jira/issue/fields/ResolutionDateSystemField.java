package com.atlassian.jira.issue.fields;

import com.atlassian.core.ofbiz.comparators.OFBizDateComparator;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.SearchHandler;
import com.atlassian.jira.issue.search.handlers.ResolutionDateSearchHandlerFactory;
import com.atlassian.jira.issue.search.handlers.SearchHandlerFactory;
import com.atlassian.jira.issue.statistics.DateFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.velocity.VelocityManager;

import java.util.Map;

/**
 * Stores the date an issue was resolved on.  If an issue is changed back into the unresolved state, this
 * field returns nothing again.
 *
 * @since v4.0
 */
public class ResolutionDateSystemField extends NavigableFieldImpl implements SearchableField, DateField
{
    private final SearchHandlerFactory searchHandlerFactory;
    private final ColumnViewDateTimeHelper columnViewDateTimeHelper;

    public ResolutionDateSystemField(VelocityManager velocityManager, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, ResolutionDateSearchHandlerFactory searchHandlerFactory, ColumnViewDateTimeHelper columnViewDateTimeHelper)
    {
        super(IssueFieldConstants.RESOLUTION_DATE, "issue.field.resolution.date", "issue.column.heading.resolution.date", ORDER_DESCENDING, new OFBizDateComparator(IssueFieldConstants.RESOLUTION_DATE), velocityManager, applicationProperties, authenticationContext);
        this.searchHandlerFactory = searchHandlerFactory;
        this.columnViewDateTimeHelper = columnViewDateTimeHelper;
    }

    public LuceneFieldSorter getSorter()
    {
        return DateFieldSorter.ISSUE_RESOLUTION_DATE_STATSMAPPER;
    }

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue)
    {
        return columnViewDateTimeHelper.render(this, fieldLayoutItem, displayParams, issue, issue.getResolutionDate());
    }

    public SearchHandler createAssociatedSearchHandler()
    {
        return searchHandlerFactory.createHandler(this);
    }
}