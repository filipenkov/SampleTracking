package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
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
import org.ofbiz.core.entity.GenericValue;

import java.util.Map;

/**
 * Fragment to display unresolved issues by components in the Issues Project Tab Panel
 *
 * @since v4.0
 */
public class UnresolvedIssuesByComponentFragment extends AbstractUnresolvedIssuesFragment implements ProjectTabPanelFragment
{
    private static final Logger log = Logger.getLogger(UnresolvedIssuesByComponentFragment.class);
    private final ApplicationProperties applicationProperties;

    public UnresolvedIssuesByComponentFragment(final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties, final JiraAuthenticationContext authenticationContext)
    {
        super(templatingEngine, authenticationContext);
        this.applicationProperties = applicationProperties;
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "unresolvedissuesbycomponent";
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("components", getComponents(ctx));
        velocityParams.put("urlUtil", new ComponentUrlUtil(getSearchRequest(ctx), authenticationContext.getLoggedInUser(), applicationProperties));
        return velocityParams;
    }

    String getIssueFieldConstant()
    {
        return IssueFieldConstants.COMPONENTS;
    }

    StatisticMapWrapper getComponents(final BrowseContext ctx)
    {
        try
        {
            return getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.COMPONENTS);
        }
        catch (SearchException e)
        {
            log.error("Could not search for priorities in project '" + ctx.getProject().getKey() + "'", e);
            return null;
        }
    }

    static public class ComponentUrlUtil extends AbstractUrlFragmentUtil<GenericValue>
    {
        public ComponentUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }

        protected Clause getDomainClause(final GenericValue component)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            if (component != null)
            {
                builder.component().eq(component.getString("name"));
            }
            else
            {
                builder.componentIsEmpty();
            }
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.component(SortOrder.ASC);
            return jqlOrderByBuilder.buildOrderBy();
        }
    }
}
