package com.atlassian.streams.jira.search;

import java.util.Collection;
import java.util.Set;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.streams.api.ActivityRequest;
import com.atlassian.streams.api.StreamsFilterType.Operator;
import com.atlassian.streams.api.common.Pair;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSet;

import static com.atlassian.streams.spi.Filters.getIsValues;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.USER;
import static com.atlassian.streams.spi.StandardStreamsFilterOption.PROJECT_KEY;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Predicates.and;
import static com.google.common.base.Predicates.notNull;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Iterables.isEmpty;
import static com.google.common.collect.Iterables.transform;

class UserHistory
{
    private final ProjectManager projectManager;
    private final PermissionManager permissionManager;
    private final JiraAuthenticationContext authenticationContext;
    private final ChangeHistoryManager changeHistoryManager;

    public UserHistory(ProjectManager projectManager,
            PermissionManager permissionManager,
            JiraAuthenticationContext authenticationContext,
            ChangeHistoryManager changeHistoryManager)
    {
        this.projectManager = checkNotNull(projectManager, "projectManager");
        this.permissionManager = checkNotNull(permissionManager, "permissionManager");
        this.authenticationContext = checkNotNull(authenticationContext, "authenticationContext");
        this.changeHistoryManager = checkNotNull(changeHistoryManager, "changeHistoryManager");
    }
    
    public Set<Issue> find(final ActivityRequest request)
    {
        Collection<Project> projects = ImmutableSet.copyOf(getProjects(request));
        if (projects.isEmpty())
        {
            // If there are no projects then that means the user doesn't have permission to browse the requested project
            return ImmutableSet.of();
        }
        return ImmutableSet.copyOf(changeHistoryManager.findUserHistory(
                authenticationContext.getLoggedInUser(), 
                getUsers(request),
                projects,
                request.getMaxResults()));
    }

    private Collection<String> getUsers(ActivityRequest request)
    {
        Collection<Pair<Operator, Iterable<String>>> filters = request.getStandardFilters().get(USER.getKey());
        if (filters.isEmpty())
        {
            // ChangeHistoryManager is a bit stupid and will add a "reporter in ()" clause if the user list is
            // not null and empty.  So we return null here to signal it shouldn't add any reporter clause.
            return null;
        }
        Collection<String> result = getIsValues(filters);
        if (result.isEmpty())
        {
            // See above
            return null;
        }
        else
        {
            return result;
        }
    }

    private Iterable<Project> getProjects(ActivityRequest request)
    {
        Iterable<String> projectKeys = getIsValues(request.getStandardFilters().get(PROJECT_KEY));

        if (!isEmpty(projectKeys))
        {
            return filter(transform(projectKeys, toProject), and(notNull(), hasPermission));
        }
        else
        {
            return permissionManager.getProjectObjects(Permissions.BROWSE, authenticationContext.getLoggedInUser());
        }
    }
    
    private final Function<String, Project> toProject = new Function<String, Project>()
    {
        public Project apply(String key)
        {
            return projectManager.getProjectObjByKey(key);
        }
    };

    private final Predicate<Project> hasPermission = new Predicate<Project>()
    {
        public boolean apply(Project project)
        {
            return permissionManager.hasPermission(Permissions.BROWSE, project, authenticationContext.getLoggedInUser());
        }
    };
}
