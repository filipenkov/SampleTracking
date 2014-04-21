package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlOrderByBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SortOrder;
import org.apache.log4j.Logger;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Displays a breakdown of all the unresolved issues in the instance, grouped by priority.
 *
 * @since v4.0
 */
public class UnresolvedIssuesByPriorityFragment extends AbstractUnresolvedIssuesFragment implements ProjectTabPanelFragment
{
    private static final Logger log = Logger.getLogger(UnresolvedIssuesByPriorityFragment.class);

    private final ApplicationProperties applicationProperties;
    private final ConstantsManager constantsManager;

    public UnresolvedIssuesByPriorityFragment(final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext,
            final ConstantsManager constantsManager)
    {
        super(templatingEngine, authenticationContext);
        this.applicationProperties = applicationProperties;
        this.constantsManager = constantsManager;
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "unresolvedissuesbypriority";
    }

    String getIssueFieldConstant()
    {
        return IssueFieldConstants.PRIORITY;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("priorities", getPriorities(ctx));
        velocityParams.put("urlUtil", new PriorityUrlUtil(getSearchRequest(ctx), authenticationContext.getLoggedInUser(), applicationProperties));
        return velocityParams;
    }

    StatisticMapWrapper getPriorities(final BrowseContext ctx)
    {
        try
        {
            // StatisticMapWrapper contains GV keys; we want Priority keys
            StatisticMapWrapper map = getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.PRIORITIES);
            transformStatisticMapWrapper(map);
            return map;
        }
        catch (SearchException e)
        {
            log.error("Could not search for priorities in project '" + ctx.getProject().getKey() + "'", e);
        }
        
        return null;
    }

    void addSearchSorts(final JqlQueryBuilder builder)
    {
        // for the Priority fragment, we want Priority first then Key
        builder.orderBy().issueKey(SortOrder.DESC, true);
    }

    private void transformStatisticMapWrapper(final StatisticMapWrapper map)
    {
        Map<Priority, Integer> priorityMap = new LinkedHashMap<Priority, Integer>(map.size());
        for (Object o : map.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            Priority priority = (Priority) entry.getKey();
            priorityMap.put(priority, (Integer) entry.getValue());

        }
        map.setStatistics(priorityMap);
    }

    static public class PriorityUrlUtil extends AbstractUrlFragmentUtil<Priority>
    {
        public PriorityUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }

        protected Clause getDomainClause(final Priority priority)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            builder.priority().eq(priority.getName());
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.priority(SortOrder.DESC);
            return jqlOrderByBuilder.buildOrderBy();
        }


    }

}
