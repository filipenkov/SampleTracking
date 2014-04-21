package com.atlassian.jira.plugin.projectpanel.fragment.impl;

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
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.bean.StatisticMapWrapper;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SortOrder;
import com.atlassian.velocity.VelocityManager;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Fragment to display unresolved issues by fix version in the Issues Project Tab Panel
 *
 * @since v4.0
 */
public class UnresolvedIssuesByFixVersionFragment extends AbstractUnresolvedIssuesFragment implements ProjectTabPanelFragment
{
    private static final Logger log = Logger.getLogger(UnresolvedIssuesByFixVersionFragment.class);

    public UnresolvedIssuesByFixVersionFragment(final VelocityManager velocityManager, final ApplicationProperties applicationProperites, final JiraAuthenticationContext jiraAuthenticationContext)
    {
        super(velocityManager, applicationProperites, jiraAuthenticationContext);
    }

    // NOTE: We need to ensure that all IDs are unique (as required by HTML specification).
    // When implementing ProjectTabPanelFragment ensure you add it to TestProjectTabPanelFragment test!
    public String getId()
    {
        return "unresolvedissuesbyfixversion";
    }

    protected Map<String, Object> createVelocityParams(final BrowseContext ctx)
    {
        final Map<String, Object> velocityParams = super.createVelocityParams(ctx);
        velocityParams.put("versions", getVersions(ctx));
        velocityParams.put("urlUtil", new FixVersionUrlUtil(getSearchRequest(ctx), jiraAuthenticationContext.getUser(), applicationProperites));
        return velocityParams;
    }

    String getIssueFieldConstant()
    {
        return IssueFieldConstants.FIX_FOR_VERSIONS;
    }

    StatisticMapWrapper getVersions(final BrowseContext ctx)
    {
        try
        {
            return getStatsBean(ctx).getAllFilterBy(FilterStatisticsValuesGenerator.ALLFIXFOR);
        }
        catch (SearchException e)
        {
            log.error("Could not search for versions in project '" + ctx.getProject().getKey() + "'", e);
            return null;
        }
    }

    @Override
    public boolean showFragment(BrowseContext ctx)
    {
        return (!ctx.getProject().getVersions().isEmpty()) && super.showFragment(ctx);
    }

    static public class FixVersionUrlUtil extends AbstractUrlFragmentUtil<Version>
    {
        public FixVersionUrlUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
        {
           super(searchRequest, user, applicationProperties);
        }

        protected Clause getDomainClause(final Version version)
        {
            JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
            if (version != null)
            {
                builder.fixVersion().eq(version.getName());
            }
            else
            {
                builder.fixVersionIsEmpty();
            }
            return builder.buildClause();
        }

        protected OrderBy getOrderBy()
        {
            JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
            jqlOrderByBuilder.fixForVersion(SortOrder.ASC);
            return jqlOrderByBuilder.buildOrderBy();
        }
    }
}