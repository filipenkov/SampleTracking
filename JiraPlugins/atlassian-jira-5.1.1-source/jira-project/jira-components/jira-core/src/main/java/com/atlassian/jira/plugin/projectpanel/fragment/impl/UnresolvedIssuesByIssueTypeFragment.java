package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.issuetype.IssueType;
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

import java.util.Map;

/**
 * Displays a breakdown of all the unresolved issues in the instance, grouped by issue type.
 *
 * @since v5.1
 */
public class UnresolvedIssuesByIssueTypeFragment extends AbstractUnresolvedIssuesFragment implements ProjectTabPanelFragment
{
    private static final Logger log = Logger.getLogger(UnresolvedIssuesByIssueTypeFragment.class);
    private static final String FRAGMENT_ID = "unresolvedissuesbyissuetype";

    private final ApplicationProperties applicationProperties;
    private final ConstantsManager constantsManager;

    public UnresolvedIssuesByIssueTypeFragment(final VelocityTemplatingEngine templatingEngine,
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
        return FRAGMENT_ID;
    }

    String getIssueFieldConstant()
    {
        return IssueFieldConstants.ISSUE_TYPE;
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("issueTypes", getIssueTypes(ctx));
        velocityParams.put("urlUtil", new IssueTypeUrlUtil(getSearchRequest(ctx), authenticationContext.getLoggedInUser(), applicationProperties));
        return velocityParams;
    }

    StatisticMapWrapper getIssueTypes(final BrowseContext ctx)
    {
        try
        {
            StatisticMapWrapper map = getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.ISSUETYPE);
            return map;
        }
        catch (SearchException e)
        {
            log.error("Could not search for issue types in project '" + ctx.getProject().getKey() + "'", e);
        }
        
        return null;
    }

    static public class IssueTypeUrlUtil extends AbstractUrlFragmentUtil<IssueType>
    {
        public IssueTypeUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }


        protected Clause getDomainClause(final IssueType issueType)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            builder.issueType().eq(issueType.getName());
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.issueType(SortOrder.ASC);
            return jqlOrderByBuilder.buildOrderBy();
        }


    }

}
