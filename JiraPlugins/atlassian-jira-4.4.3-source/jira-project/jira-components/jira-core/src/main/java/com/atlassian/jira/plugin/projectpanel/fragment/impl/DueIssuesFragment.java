package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.query.order.SortOrder;
import com.atlassian.velocity.VelocityManager;

/**
 * The fragment which displays the earliest issues due
 *
 * @since v4.0
 */
public class DueIssuesFragment extends AbstractIssuesFragment
{
    public DueIssuesFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites, final JiraAuthenticationContext jiraAuthenticationContext, final SearchProvider searchProvider, final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(jiraAuthenticationContext, velocityManager, searchProvider, applicationProperites, dateTimeFormatterFactory);
    }

    public String getId()
    {
        return "dueissues";
    }

    @Override
    public boolean showFragment(BrowseContext ctx)
    {
        final FieldVisibilityManager fieldVisibilityManager = new FieldVisibilityBean();
        return super.showFragment(ctx) && !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), IssueFieldConstants.DUE_DATE);
    }

    SearchRequest getSearchRequest(final BrowseContext ctx)
    {
        final JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(ctx.createQuery());
        queryBuilder.where().defaultAnd().unresolved();
        queryBuilder.orderBy().clear().dueDate(SortOrder.ASC).priority(SortOrder.DESC).createdDate(SortOrder.ASC);
        return new SearchRequest(queryBuilder.buildQuery());
    }
}