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
import com.atlassian.jira.plugin.profile.UserFormatManager;
import com.atlassian.jira.plugin.projectpanel.fragment.ProjectTabPanelFragment;
import com.atlassian.jira.plugin.userformat.FullNameUserFormat;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SortOrder;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Fragment to display unresolved issues by assignee in the Issues Project Tab Panel
 *
 * @since v4.0
 */
public class UnresolvedIssuesByAssigneeFragment extends AbstractUnresolvedIssuesFragment implements ProjectTabPanelFragment
{
    private static final Logger log = Logger.getLogger(UnresolvedIssuesByAssigneeFragment.class);

    private final ApplicationProperties applicationProperties;
    private final UserFormatManager userFormatManager;

    public UnresolvedIssuesByAssigneeFragment(final VelocityTemplatingEngine templatingEngine,
            final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext jiraAuthenticationContext, final UserFormatManager userFormatManager)
    {
        super(templatingEngine, jiraAuthenticationContext);
        this.applicationProperties = applicationProperties;
        this.userFormatManager = userFormatManager;
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "unresolvedissuesbyassignee";
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("userformat", userFormatManager.getUserFormat(FullNameUserFormat.TYPE));
        StatisticMapWrapper assignees = getAssignees(ctx);
        velocityParams.put("assignees", assignees);
        velocityParams.put("urlUtil", new AssigneeUrlUtil(getSearchRequest(ctx), authenticationContext.getLoggedInUser(), applicationProperties));
        return velocityParams;
    }

    String getIssueFieldConstant()
    {
        return IssueFieldConstants.ASSIGNEE;
    }

    StatisticMapWrapper getAssignees(final BrowseContext ctx)
    {
        try
        {
            return getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.ASSIGNEES);
        }
        catch (SearchException e)
        {
            log.error("Could not search for assignees in project '" + ctx.getProject().getKey() + "'", e);
        }
        return null;
    }

    static public class AssigneeUrlUtil extends AbstractUrlFragmentUtil<User>
    {
        public AssigneeUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }

        protected Clause getDomainClause(final User user)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            if (user != null)
            {
                builder.assignee().eq(user.getName());
            }
            else
            {
                builder.assigneeIsEmpty();
            }
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.assignee(SortOrder.ASC);
            return jqlOrderByBuilder.buildOrderBy();
        }
    }
}