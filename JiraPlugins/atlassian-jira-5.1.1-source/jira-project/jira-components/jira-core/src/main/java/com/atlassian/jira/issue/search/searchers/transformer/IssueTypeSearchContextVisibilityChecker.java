package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * A {@link SearchContextVisibilityChecker} for issue types
 *
 * @since v4.0
 */
public class IssueTypeSearchContextVisibilityChecker implements SearchContextVisibilityChecker
{
    private final ProjectManager projectManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;

    public IssueTypeSearchContextVisibilityChecker(ProjectManager projectManager, IssueTypeSchemeManager issueTypeSchemeManager)
    {
        this.projectManager = projectManager;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
    }

    public Set<String> FilterOutNonVisibleInContext(final SearchContext searchContext, final Collection<String> ids)
    {
        if (searchContext.isForAnyProjects())
        {
            return new HashSet<String>(ids);
        }
        else
        {
            final Set<String> types = new HashSet<String>();
            for (Long pid : searchContext.getProjectIds())
            {
                final Project project = projectManager.getProjectObj(pid);
                if (project != null)
                {
                    final Collection<IssueType> issueTypes = issueTypeSchemeManager.getIssueTypesForProject(project);
                    for (IssueType issueType : issueTypes)
                    {
                        if (ids.contains(issueType.getId()))
                        {
                            types.add(issueType.getId());
                        }
                    }
                }
            }
            return types;
        }
    }
}