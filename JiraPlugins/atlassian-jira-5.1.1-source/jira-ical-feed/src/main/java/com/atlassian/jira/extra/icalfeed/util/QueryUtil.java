package com.atlassian.jira.extra.icalfeed.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.query.Query;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class QueryUtil
{
    private final SearchService searchService;

    private final ProjectManager projectManager;

    private final PermissionManager permissionManager;

    public QueryUtil(SearchService searchService, ProjectManager projectManager, PermissionManager permissionManager)
    {
        this.searchService = searchService;
        this.projectManager = projectManager;
        this.permissionManager = permissionManager;
    }

    public Set<Project> getBrowseableProjectsFromQuery(final User user, Query searchQuery)
    {
        QueryContext simpleQueryContext = searchService.getSimpleQueryContext(user, searchQuery);
        Collection queryProjects = simpleQueryContext.getProjectIssueTypeContexts();
        Set<Project> projectsInQuery = new HashSet<Project>();

        for (Object contextObject : queryProjects)
        {
            projectsInQuery.addAll(
                    Collections2.filter(
                            Collections2.transform(
                                    ((QueryContext.ProjectIssueTypeContexts) contextObject).getProjectIdInList(),
                                    new Function<Long, Project>()
                                    {
                                        public Project apply(Long projectId)
                                        {
                                            return projectManager.getProjectObj(projectId);
                                        }
                                    }
                            ),
                            Predicates.and(Predicates.<Project>notNull(), new Predicate<Project>()
                            {
                                public boolean apply(Project aProject)
                                {
                                    return permissionManager.hasPermission(Permissions.BROWSE, aProject, user);
                                }
                            })
                    )
            );
        }

        return projectsInQuery;
    }
}
