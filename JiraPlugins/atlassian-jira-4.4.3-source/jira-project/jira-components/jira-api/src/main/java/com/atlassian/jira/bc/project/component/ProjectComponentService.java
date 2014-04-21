package com.atlassian.jira.bc.project.component;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.util.ErrorCollection;

import java.util.Collection;

public interface ProjectComponentService
{
    ProjectComponent create(User user, ErrorCollection errorCollection, String name, String description, String lead, Long projectId);

    ProjectComponent create(com.opensymphony.user.User user, ErrorCollection errorCollection, String name, String description, String lead, Long projectId);

    ProjectComponent find(User user, ErrorCollection errorCollection, Long id);

    ProjectComponent find(com.opensymphony.user.User user, ErrorCollection errorCollection, Long id);

    /**
     * @deprecated Use {@link com.atlassian.jira.bc.project.component.ProjectComponentService#find(User, com.atlassian.jira.util.ErrorCollection, Long)}
     * instead. This methods completely ignores the user calling and thus serves components for such project
     * a person may have no access rights to see
     *
     * @param errorCollection collection to add error messages to if validation and permission checks fail - String objects
     * @param id component id
     * @return project component or null and then <code>errorCollection</code> should contain some error information
     */
    @Deprecated
    ProjectComponent find(ErrorCollection errorCollection, Long id);

    Collection<ProjectComponent> findAllForProject(ErrorCollection errorCollection, Long projectId);

    ProjectComponent update(User user, ErrorCollection errorCollection, MutableProjectComponent component);

    ProjectComponent update(com.opensymphony.user.User user, ErrorCollection errorCollection, MutableProjectComponent component);

    void deleteComponentForIssues(JiraServiceContext context, Long componentId);

    void deleteAndSwapComponentForIssues(JiraServiceContext context, Long componentId, Long swapComponentId);
}
