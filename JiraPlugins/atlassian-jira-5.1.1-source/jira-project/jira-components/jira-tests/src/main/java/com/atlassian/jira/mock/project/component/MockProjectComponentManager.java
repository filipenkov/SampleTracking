package com.atlassian.jira.mock.project.component;

import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.MutableProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.issue.Issue;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public class MockProjectComponentManager implements ProjectComponentManager
{
    public ProjectComponent create(String name, String description, String lead, long assigneeType, Long projectId) throws IllegalArgumentException
    {
        return null;
    }

    public ProjectComponent find(Long id) throws EntityNotFoundException
    {
        return null;
    }

    public Collection<ProjectComponent> findAllForProject(Long projectId)
    {
        return null;
    }

    public Collection<ProjectComponent> findAll()
    {
        return null;
    }

    public List<ProjectComponent> getComponents(final List<Long> ids) throws EntityNotFoundException
    {
        throw new UnsupportedOperationException("method not implemented");
    }

    public ProjectComponent update(MutableProjectComponent component) throws EntityNotFoundException
    {
        return null;
    }

    public void delete(Long componentId) throws EntityNotFoundException
    {
    }

    public boolean containsName(String name, Long projectId)
    {
        return false;
    }

    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException
    {
        return null;
    }

    public GenericValue convertToGenericValue(ProjectComponent projectComponent)
    {
        return null;
    }

    public Collection<GenericValue> convertToGenericValues(Collection<ProjectComponent> projectComponents)
    {
        return null;
    }

    public ProjectComponent findByComponentName(Long projectId, String componentName)
    {
        return null;
    }

    public Collection<ProjectComponent> findByComponentNameCaseInSensitive(final String componentName)
    {
        return null;
    }

    public Collection<ProjectComponent> findComponentsByLead(String userName)
    {
        return null;
    }

    public Collection<ProjectComponent> findComponentsByIssue(final Issue issue)
    {
        return null;
    }

    public Collection<GenericValue> findComponentsByIssueGV(final Issue issue)
    {
        return null;
    }
}
