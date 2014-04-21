package com.atlassian.jira.plugin.projectpanel.fragment.impl;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.views.util.SearchRequestViewUtils;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlOrderByBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.util.velocity.DefaultVelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.query.Query;
import com.atlassian.query.QueryImpl;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.order.OrderBy;
import com.atlassian.query.order.SearchSort;
import com.opensymphony.user.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class for building the urls required by the fragments
 *
 * @since v4.0
 */
public abstract class AbstractUrlFragmentUtil<T>
{
    private final SearchRequest searchRequest;
    private final User user;
    private final String baseUrl;
    private String allUrl;

    public AbstractUrlFragmentUtil(final SearchRequest searchRequest, final User user, final ApplicationProperties applicationProperties)
    {
        this.searchRequest = searchRequest;
        this.user = user;
        this.baseUrl = createBaseUrl(applicationProperties);
        this.allUrl = null;
    }

    /**
     * Create the clause to search for a specific domain object
     * @param domain the domain object that needs to be searched for e.g. {@link com.atlassian.jira.project.version.Version}. Can be Null.
     * @return the clause that searchs for issues with the field equal to the domain objects value, or if the domain object is Null, is empty. Never Null.
     */
    protected abstract Clause getDomainClause(T domain);

    /**
     * @return the order by clause that sorts by the field
     */
    protected abstract OrderBy getOrderBy();

    public String getUrl(T domain)
    {
        final Query orgQuery = searchRequest.getQuery();
        JqlClauseBuilder builder = JqlQueryBuilder.newClauseBuilder();
        if (orgQuery.getWhereClause() != null)
        {
            builder.addClause(orgQuery.getWhereClause()).and();
        }
        builder.addClause(getDomainClause(domain));

        return getLink(builder.buildClause(), orgQuery.getOrderByClause());
    }

    public String getAllUrl()
    {
        if (allUrl == null)
        {
            allUrl = createAllUrl();
        }
        return allUrl;
    }

    String createAllUrl()
    {
        final Query orgQuery = searchRequest.getQuery();
        JqlOrderByBuilder jqlOrderByBuilder = JqlQueryBuilder.newOrderByBuilder();
        List<SearchSort> sorts = new ArrayList<SearchSort>();
        // make the specific sort first
        sorts.addAll(getOrderBy().getSearchSorts());
        if (orgQuery.getOrderByClause() != null)
        {
            sorts.addAll(orgQuery.getOrderByClause().getSearchSorts());
        }
        jqlOrderByBuilder.setSorts(sorts);

        return getLink(orgQuery.getWhereClause(), jqlOrderByBuilder.buildOrderBy());
    }
    ///CLOVER:OFF
    String getLink(final Clause whereClause, final OrderBy orderBy)
    {
        SearchRequest newSearchRequest = new SearchRequest(searchRequest);
        newSearchRequest.setQuery(new QueryImpl(whereClause, orderBy, null));

        final StringBuilder link = new StringBuilder();
        link.append(SearchRequestViewUtils.getLink(newSearchRequest, baseUrl, user))
                .append("&mode=hide");
        return link.toString();
    }

    String createBaseUrl(final ApplicationProperties applicationProperties)
    {
        final VelocityRequestContext velocityRequestContext = new DefaultVelocityRequestContextFactory(applicationProperties).getJiraVelocityRequestContext();
        return velocityRequestContext.getBaseUrl();
    }
    ///CLOVER:ON
}
