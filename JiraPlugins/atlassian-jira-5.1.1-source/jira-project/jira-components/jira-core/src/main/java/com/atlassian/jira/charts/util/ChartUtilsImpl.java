package com.atlassian.jira.charts.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Utility class for charting
 *
 * @since v4.0
 */
public class ChartUtilsImpl implements ChartUtils
{
    private static final Logger log = Logger.getLogger(ChartUtilsImpl.class);

    private final JiraAuthenticationContext authenticationContext;
    private final ProjectManager projectManager;
    private final SearchService searchService;
    private final SearchRequestService searchRequestService;

    public ChartUtilsImpl(SearchRequestService searchRequestService, JiraAuthenticationContext authenticationContext,
            ProjectManager projectManager, final SearchService searchService)
    {
        this.searchRequestService = searchRequestService;
        this.authenticationContext = authenticationContext;
        this.projectManager = projectManager;
        this.searchService = searchService;
    }


    public SearchRequest retrieveOrMakeSearchRequest(final String projectOrFilterId, final Map<String, Object> params)
    {
        SearchRequest sr = null;

        final User user = authenticationContext.getLoggedInUser();
        if (projectOrFilterId.startsWith("filter-"))
        {
            Long filterId = new Long(projectOrFilterId.substring(7));
            sr = searchRequestService.getFilter(
                    new JiraServiceContextImpl(user, new SimpleErrorCollection()), filterId);
            if (sr != null)
            {
                params.put("searchRequest", sr);
            }
        }
        else if (projectOrFilterId.startsWith("project-"))
        {
            Long projectId = new Long(projectOrFilterId.substring(8));
            final Project project = projectManager.getProjectObj(projectId);
            if (project != null)
            {
                sr = makeProjectSearchRequest(project.getKey());
                params.put("project", project);
            }
        }
        else if(projectOrFilterId.startsWith("jql-"))
        {
            final String jql = projectOrFilterId.substring(4);

            sr = new SearchRequest();
            if (StringUtils.isNotBlank(jql))
            {
                final SearchService.ParseResult parseResult = searchService.parseQuery(user, jql);
                if (parseResult.isValid())
                {
                    sr = new SearchRequest(parseResult.getQuery());
                }
                else
                {
                    throw new IllegalArgumentException("Invalid JQL query specified for chart '" + jql + "'.");
                }
            }
            params.put("searchRequest", sr);
        }

        return sr;
    }

    private SearchRequest makeProjectSearchRequest(String projectKey)
    {
        return new SearchRequest(JqlQueryBuilder.newBuilder().where().project(projectKey).buildQuery());
    }
}
