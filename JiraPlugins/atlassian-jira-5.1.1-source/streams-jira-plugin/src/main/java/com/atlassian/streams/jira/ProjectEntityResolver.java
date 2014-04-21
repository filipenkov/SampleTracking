package com.atlassian.streams.jira;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.streams.api.common.Option;
import com.atlassian.streams.spi.EntityResolver;

import static com.google.common.base.Preconditions.checkNotNull;

public class ProjectEntityResolver implements EntityResolver
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final ProjectService projectService;

    public ProjectEntityResolver(JiraAuthenticationContext jiraAuthenticationContext, ProjectService projectService)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.projectService = checkNotNull(projectService);
    }

    public Option<Object> apply(String key)
    {
        User loggedInUser = jiraAuthenticationContext.getLoggedInUser();

        ProjectService.GetProjectResult projectByKey = projectService.getProjectByKey(loggedInUser, key);

        Option<Object> option;
        if (projectByKey.isValid())
        {
            option = Option.<Object>some(projectByKey.getProject());
        }
        else
        {
            option = Option.none();
        }

        return option;
    }
}