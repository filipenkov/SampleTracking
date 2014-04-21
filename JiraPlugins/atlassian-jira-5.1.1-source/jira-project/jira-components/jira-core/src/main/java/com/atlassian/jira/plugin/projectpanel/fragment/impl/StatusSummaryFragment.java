package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlOrderByBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.bean.StatisticAccessorBean;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SortOrder;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Displays a break down based on issue status.
 *
 * @since v4.0
 */
public class StatusSummaryFragment extends AbstractMultiFragment
{
    private static final Logger log = Logger.getLogger(StatusSummaryFragment.class);
    private static final String TEMPLATE_DIRECTORY_PATH = "templates/plugins/jira/projectpanels/fragments/issues/";

    private final ApplicationProperties applicationProperties;
    private final ConstantsManager constantsManager;

    public StatusSummaryFragment(final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext jiraAuthenticationContext, final ConstantsManager constantsManager)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.applicationProperties = applicationProperties;
        this.constantsManager = constantsManager;
    }

    public String getId()
    {
        return "statussummary";
    }

    protected String getTemplateDirectoryPath()
    {
        return TEMPLATE_DIRECTORY_PATH;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("statuses", getStatuses(ctx));
        velocityParams.put("urlUtil", new StatusUrlUtil(getSearchRequest(ctx), authenticationContext.getLoggedInUser(), applicationProperties));
        return velocityParams;
    }

    /**
     * Always returns true as the status field is always visible.
     *
     * @param ctx ignored
     * @return true
     */
    public boolean showFragment(final BrowseContext ctx)
    {
        return true;
    }

    StatisticMapWrapper getStatuses(final BrowseContext ctx)
    {
        try
        {
            // StatisticMapWrapper contains GV keys; we want Priority keys
            StatisticMapWrapper map = getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.STATUSES);
            transformStatisticMapWrapper(map);
            return map;
        }
        catch (SearchException e)
        {
            log.error("Could not search for priorities in project '" + ctx.getProject().getKey() + "'", e);
        }

        return null;
    }

    /**
     * Retrieves statistics relevant to the current {@link BrowseContext}.
     *
     * @param ctx the current context
     * @return statistics which will be used to display a breakdown of issues
     */
    StatisticAccessorBean getStatsBean(final BrowseContext ctx)
    {
        return new StatisticAccessorBean(ctx.getUser(), getSearchRequest(ctx));
    }

    /**
     * @param ctx the {@link BrowseContext} for this tab panel
     * @return the {@link SearchRequest} that query required to return the issues of interest
     */
    SearchRequest getSearchRequest(final BrowseContext ctx)
    {
        final Query initialQuery = ctx.createQuery();

        final JqlQueryBuilder builder = JqlQueryBuilder.newBuilder(initialQuery);
        // Lets modify the sort of this query
        builder.orderBy().priority(SortOrder.DESC, true);
        return new SearchRequest(builder.buildQuery());
    }

    private void transformStatisticMapWrapper(final StatisticMapWrapper map)
    {
        Map<Status, Integer> priorityMap = new LinkedHashMap<Status, Integer>(map.size());
        for (Object o : map.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            Status status = (Status) entry.getKey();
            priorityMap.put(status, (Integer) entry.getValue());
        }
        map.setStatistics(priorityMap);
    }

    static public class StatusUrlUtil extends AbstractUrlFragmentUtil<Status>
    {
        public StatusUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }

        protected Clause getDomainClause(final Status domain)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            builder.status().eq(domain.getName());
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.status(SortOrder.DESC);
            return jqlOrderByBuilder.buildOrderBy();
        }

    }
}
