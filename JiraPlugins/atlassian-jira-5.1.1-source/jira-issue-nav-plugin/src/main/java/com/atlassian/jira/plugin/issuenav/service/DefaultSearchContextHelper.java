package com.atlassian.jira.plugin.issuenav.service;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchContextImpl;
import com.atlassian.jira.issue.search.searchers.IssueSearcher;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.issue.transport.impl.FieldValuesHolderImpl;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.query.Query;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Default implementation of SearchContextHelper
 * @since v5.1
 */
public class DefaultSearchContextHelper implements SearchContextHelper
{
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final SearchService searchService;

    public DefaultSearchContextHelper(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, SearchService searchService)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;
        this.searchService = searchService;
    }

    public SearchContextWithFieldValues getSearchContextWithFieldValuesFromJqlString(final String query)
    {
        if (StringUtils.isNotBlank(query))
        {
            final SearchService.ParseResult jqlQuery = searchService.parseQuery(getLoggedInUser(), query);
            if (jqlQuery.isValid())
            {
                SearchContext searchContext = searchService.getSearchContext(getLoggedInUser(), jqlQuery.getQuery());
                FieldValuesHolder fieldValuesHolder = createFieldValuesHolderFromQuery(jqlQuery.getQuery(), searchContext);
                return new SearchContextWithFieldValues(searchContext, fieldValuesHolder);
            }
        }
        return new SearchContextWithFieldValues(createSearchContext(), new FieldValuesHolderImpl());
    }

    public SearchContext getSearchContextFromJqlString(final String query)
    {
        if (StringUtils.isNotBlank(query))
        {
            final SearchService.ParseResult jqlQuery = searchService.parseQuery(getLoggedInUser(), query);
            if (jqlQuery.isValid())
            {
                return searchService.getSearchContext(getLoggedInUser(), jqlQuery.getQuery());
            }
        }
        return createSearchContext();
    }

    private FieldValuesHolder createFieldValuesHolderFromQuery(final Query query, final SearchContext searchContext)
    {
        FieldValuesHolder fieldValuesHolder = new FieldValuesHolderImpl();
        final Collection<IssueSearcher<?>> searchers = new ArrayList<IssueSearcher<?>>(); // TODO
        for (IssueSearcher<?> searcher : searchers)
        {
            searcher.getSearchInputTransformer().populateFromQuery(getLoggedInUser(), fieldValuesHolder, query, searchContext);
        }
        return fieldValuesHolder;
    }

    private SearchContext createSearchContext()
    {
        Collection<Project> visibleProjects = permissionManager.getProjectObjects(Permissions.BROWSE, getLoggedInUser());
        if (null == visibleProjects)
        {
            return new SearchContextImpl();
        }
        else
        {
            List<Long> projectIds = new ArrayList<Long>(visibleProjects.size());
            for (Project project : visibleProjects)
            {
                projectIds.add(project.getId());
            }
            return new SearchContextImpl(null, projectIds, null);
        }
    }

    private User getLoggedInUser()
    {
        return authenticationContext.getLoggedInUser();
    }
}
