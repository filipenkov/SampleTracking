package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.datetime.DateTimeFormatterFactory;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;

/**
 * The fragment which displays the most recently updated issues
 *
 * @since v4.0
 */
public class RecentIssuesFragment extends AbstractIssuesFragment
{
    public RecentIssuesFragment(final JiraAuthenticationContext jiraAuthenticationContext, final VelocityTemplatingEngine templatingEngine, final SearchProvider searchProvider, final ApplicationProperties applicationProperites, final DateTimeFormatterFactory dateTimeFormatterFactory)
    {
        super(jiraAuthenticationContext, templatingEngine, searchProvider, applicationProperites, dateTimeFormatterFactory);
    }

    public String getId()
    {
        return "recentissues";
    }

    SearchRequest getSearchRequest(final BrowseContext ctx)
    {
        final Query query = ctx.createQuery();

        JqlQueryBuilder queryBuilder = JqlQueryBuilder.newBuilder(query);
        queryBuilder.orderBy().updatedDate(SortOrder.DESC).priority(SortOrder.DESC).createdDate(SortOrder.ASC);

        return new SearchRequest(queryBuilder.buildQuery());
    }

}