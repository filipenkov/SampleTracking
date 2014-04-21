package com.atlassian.jira.bc.project.component;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

@PublicApi
public interface ProjectComponentService
{
    ProjectComponent create(User user, ErrorCollection errorCollection, String name, String description, String lead, Long projectId);

    ProjectComponent find(User user, ErrorCollection errorCollection, Long id);

    Collection<ProjectComponent> findAllForProject(ErrorCollection errorCollection, Long projectId);

    ProjectComponent update(User user, ErrorCollection errorCollection, MutableProjectComponent component);

    void deleteComponentForIssues(JiraServiceContext context, Long componentId);

    void deleteAndSwapComponentForIssues(JiraServiceContext context, Long componentId, Long swapComponentId);
}
