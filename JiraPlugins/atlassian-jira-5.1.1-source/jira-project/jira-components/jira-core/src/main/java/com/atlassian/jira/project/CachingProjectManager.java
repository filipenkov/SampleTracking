/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.bc.project.component.ProjectComponentManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.comparator.ComponentComparator;
import com.atlassian.jira.user.util.UserManager;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@EventComponent
public class CachingProjectManager extends AbstractProjectManager
{
    private final ProjectManager delegateProjectManager;
    private final ProjectComponentManager projectComponentManager;
    private final ProjectFactory projectFactory;
    private final ProjectCache cache;

    public CachingProjectManager(ProjectManager delegateProjectManager, ProjectComponentManager projectComponentManager,
            ProjectFactory projectFactory, UserManager userManager, ApplicationProperties applicationProperties)
    {
        super(userManager, applicationProperties);
        this.delegateProjectManager = delegateProjectManager;
        this.projectComponentManager = projectComponentManager;
        this.projectFactory = projectFactory;
        this.cache = new ProjectCache(true);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        updateCache();
        //make sure the project count cache is reloaded (the delegate may also do some caching)
        delegateProjectManager.refresh();
    }

    public void updateCache()
    {
        // refresh the project cache
        cache.refresh();
    }

    // Business Methods ------------------------------------------------------------------------------------------------
    // Create Methods --------------------------------------------------------------------------------------------------
    @Override
    public long getNextId(Project project)
    {
        long nextId = delegateProjectManager.getNextId(project);
        return nextId;
    }

    @Override
    public void refresh()
    {
        updateCache();
        delegateProjectManager.refresh();
    }

    // Get / Finder Methods --------------------------------------------------------------------------------------------
    @Override
    public GenericValue getProject(Long id)
    {
        return cache.getProject(id);
    }

    @Override
    public Project getProjectObj(Long id)
    {
        Project project = null;
        GenericValue projectGv = cache.getProject(id);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    @Override
    public GenericValue getProjectByName(String name)
    {
        return cache.getProjectByName(name);
    }

    @Override
    public GenericValue getProjectByKey(String key)
    {
        return cache.getProjectByKey(key);
    }

    @Override
    public Project getProjectObjByKey(String projectKey)
    {
        Project project = null;
        GenericValue projectGv = getProjectByKey(projectKey);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    @Override
    public Project getProjectObjByKeyIgnoreCase(final String projectKey)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByKey(projectKey);
        if (projectGv == null)
        {
            // Try to run through all the projects and compare on the key
            for (Project prj : getProjectObjects())
            {
                if (prj.getKey().equalsIgnoreCase(projectKey))
                {
                    project = prj;
                    break;
                }
            }
        }
        else
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    @Override
    public Project getProjectObjByName(String projectName)
    {
        Project project = null;
        GenericValue projectGv = getProjectByName(projectName);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    /**
     * Retrieves the Component with the given name in the given Project or null
     * if none match.
     *
     * @param project the Project.
     * @param name    the Component name.
     * @return the Component as a GenericValue or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public GenericValue getComponent(GenericValue project, String name)
    {
        return projectComponentManager.convertToGenericValue(projectComponentManager.findByComponentName(project.getLong("id"), name));
    }

    /**
     * Retrieves the Component (as a GenericValue) with the given id or null
     * if none match.
     *
     * @param id the id of the component to retrieve
     * @return the Component as a GenericValue or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public GenericValue getComponent(Long id)
    {
        try
        {
            return projectComponentManager.convertToGenericValue(projectComponentManager.find(id));
        }
        catch (EntityNotFoundException e)
        {
            return null;
        }
    }

    /**
     * Retrieve the collection of Components (as GenericValues) associated with the specified
     * project.
     *
     * @param project the project (as a GenericValue) used to search on
     * @return collection of components (as GenericValues) or null if there is no match.
     * @deprecated use ProjectComponentManager and ProjectComponent
     */
    @Override
    public Collection getComponents(GenericValue project)
    {
        final Collection allComponentsForProject = projectComponentManager.findAllForProject(project.getLong("id"));
        List componentGVs = new ArrayList(projectComponentManager.convertToGenericValues(allComponentsForProject));
        Collections.sort(componentGVs, new ComponentComparator());
        return componentGVs;
    }

    @Override
    public Collection<GenericValue> getProjects()
    {
        return noNull(cache.getProjects());
    }

    @Override
    public List<Project> getProjectObjects()
    {
        List<Project> projects = cache.getProjectObjects();
        if (projects == null)
        {
            return Collections.emptyList();
        }
        return projects;
    }

    protected Collection noNull(Collection col)
    {
        if (col == null)
        {
            return Collections.EMPTY_LIST;
        }
        else
        {
            return col;
        }
    }

    @Override
    public Project createProject(final String name, final String key, final String description, final String lead,
            final String url, final Long assigneeType, final Long avatarId)
    {
        final Project project = delegateProjectManager.createProject(name, key, description, lead, url, assigneeType, avatarId);
        updateCache();
        return project;
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String lead, final String url, final Long assigneeType, final Long avatarId)
    {
        final Project project = delegateProjectManager.updateProject(updatedProject, name, description, lead, url, assigneeType, avatarId);
        updateCache();
        return project;
    }

    @Override
    public void removeProjectIssues(final Project project) throws RemoveException
    {
        delegateProjectManager.removeProjectIssues(project);
    }

    @Override
    public void removeProject(final Project project)
    {
        delegateProjectManager.removeProject(project);
        updateCache();
    }

    @Override
    public Collection<GenericValue> getProjectCategories()
    {
        return noNull(cache.getProjectCategories());
    }

    @Override
    public Collection<ProjectCategory> getAllProjectCategories()
    {
        return Entity.PROJECT_CATEGORY.buildList(getProjectCategories());
    }

    @Override
    public GenericValue getProjectCategory(Long id)
    {
        return cache.getProjectCategory(id);
    }

    @Override
    public ProjectCategory getProjectCategoryObject(Long id)
    {
        return Entity.PROJECT_CATEGORY.build(cache.getProjectCategory(id));
    }

    @Override
    public void updateProjectCategory(GenericValue projectCat)
    {
        delegateProjectManager.updateProjectCategory(projectCat);
        updateCache();
    }

    @Override
    public void updateProjectCategory(ProjectCategory projectCategory) throws DataAccessException
    {
        delegateProjectManager.updateProjectCategory(projectCategory);
        updateCache();
    }

    @Override
    public Collection<GenericValue> getProjectsFromProjectCategory(GenericValue projectCategory)
    {
        return cache.getProjectsFromProjectCategory(projectCategory);
    }

    @Override
    public Collection<Project> getProjectsFromProjectCategory(ProjectCategory projectCategory)
            throws DataAccessException
    {
        return getProjectObjectsFromProjectCategory(projectCategory.getId());
    }

    @Override
    public Collection<Project> getProjectObjectsFromProjectCategory(final Long projectCategoryId)
    {
        return projectFactory.getProjects(getProjectsFromProjectCategory(getProjectCategory(projectCategoryId)));
    }

    @Override
    public GenericValue getProjectCategoryFromProject(GenericValue project)
    {
        return cache.getProjectCategoryFromProject(project);
    }

    @Override
    public ProjectCategory getProjectCategoryForProject(Project project)
    {
        GenericValue projectCategoryForProject = cache.getProjectCategoryForProject(project);
        if (projectCategoryForProject != null)
        {
            return Entity.PROJECT_CATEGORY.build(projectCategoryForProject);
        }
        return null;
    }

    @Override
    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        return cache.getProjectsWithNoCategory();
    }

    @Override
    public Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException
    {
        return projectFactory.getProjects(getProjectsWithNoCategory());
    }

    @Override
    public void setProjectCategory(GenericValue project, GenericValue category)
    {
        delegateProjectManager.setProjectCategory(project, category);
        updateCache();
    }

    @Override
    public void setProjectCategory(Project project, ProjectCategory category) throws DataAccessException
    {
        delegateProjectManager.setProjectCategory(project, category);
        updateCache();
    }

    @Override
    public List<Project> getProjectsLeadBy(User leadUser)
    {
        return delegateProjectManager.getProjectsLeadBy(leadUser);
    }

    @Override
    public Collection<GenericValue> getProjectsByLead(User leadUser)
    {
        return delegateProjectManager.getProjectsByLead(leadUser);
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        ProjectCategory projectCategory = delegateProjectManager.createProjectCategory(name, description);
        updateCache();
        return projectCategory;
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        delegateProjectManager.removeProjectCategory(id);
        updateCache();
    }

    @Override
    public long getCurrentCounterForProject(Long id)
    {
        return delegateProjectManager.getCurrentCounterForProject(id);
    }

    @Override
    public void setCurrentCounterForProject(Project project, long counter)
    {
        delegateProjectManager.setCurrentCounterForProject(project, counter);
    }
}
