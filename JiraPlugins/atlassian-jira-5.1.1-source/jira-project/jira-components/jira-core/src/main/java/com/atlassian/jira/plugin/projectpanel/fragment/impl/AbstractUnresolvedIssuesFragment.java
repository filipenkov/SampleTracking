package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.SearchSort;
import com.atlassian.query.order.SortOrder;

/**
 * Abstract fragment to display unresolved issues by XXX in the Issues Project Tab Panel
 *
 * @since v4.0
 */
abstract class AbstractUnresolvedIssuesFragment extends AbstractMultiFragment
{
    private static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/issues/";

    public AbstractUnresolvedIssuesFragment(final VelocityTemplatingEngine templatingEngine, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(templatingEngine, jiraAuthenticationContext);
    }

    public boolean showFragment(final BrowseContext ctx)
    {
        FieldVisibilityManager fieldVisibilityManager = new FieldVisibilityBean();
        return !fieldVisibilityManager.isFieldHiddenInAllSchemes(ctx.getProject().getId(), getIssueFieldConstant());
    }

    abstract String getIssueFieldConstant();

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    /**
     * Retrieves statistics relevant to the current {@link BrowseContext}.
     *
     * @param ctx the current context
     * @return statistics which will be used to display a breakdown of issues
     */
    StatisticAccessorBean getStatsBean(final BrowseContext ctx)
    {
        final SearchRequest filter = getSearchRequest(ctx);

        // remove sorting for stats generation.  Increases performance dramatically.
        Clause whereClause = filter.getQuery().getWhereClause();
        // Must set a null sort to get JIRA to search without sorts
        filter.setQuery(new QueryImpl(whereClause, null, null));

        return new StatisticAccessorBean(ctx.getUser(), filter);
    }

    /**
     * @param ctx the {@link BrowseContext} for this tab panel
     * @return the {@link SearchRequest} that query required to return the issues of interest
     */
    SearchRequest getSearchRequest(final BrowseContext ctx)
    {
        final Query initialQuery = ctx.createQuery();

        JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(initialQuery);
        // Keep what was in the initial query and append to it
        builder.where().defaultAnd().unresolved();
        // Always add a new sort
        addSearchSorts(builder);

        return new SearchRequest(builder.buildQuery());
    }

    /**
     * Adds the relevant default {@link SearchSort}s to a {@link com.atlassian.query.Query}.
     *
     * @param builder the builder to add sorts to.
     */
    void addSearchSorts(final JqlQueryBuilder builder)
    {
        // add default search sorts of the IssueFieldConstant and Priority
        builder.orderBy().priority(SortOrder.DESC, true);
    }
}
