/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.util.profiling.UtilTimerStack;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This is a very basic cache that stores projects and components
 * <p/>
 * When constructed, or when you call refresh() - it will find and cache all projects, components
 */
public class ProjectCache
{
    private static final Logger log = Logger.getLogger(ProjectCache.class);

    private static final Comparator<GenericValue> PROJECT_NAME_COMPARATOR = OfBizComparators.NAME_COMPARATOR;
    private final OfBizDelegator delegator = ComponentAccessor.getOfBizDelegator();

    // maps of projects by ID and key
    private volatile Map<Long, GenericValue> projectsById;
    private volatile Map<String, GenericValue> projectsByKey;

    // list of projectCategories
    private volatile Map<Long, GenericValue> projectCategories;

    /** Map of Project Key to Project Category ID */
    private volatile Map<String, Long> projectToProjectCategories;

    // map of projectCategory to List of Project IDs.
    private volatile Map<GenericValue, List<Long>> projectCategoriesToProjects;
    private volatile  List<Long> projectsWithNoCategory;

    // List of all Projects
    private volatile List<Project> allProjectObjects;

    public ProjectCache(boolean refresh)
    {
        if (refresh)
            refresh();
    }

    public synchronized void refresh()
    {
        if (log.isDebugEnabled())
            log.debug("ProjectCache.refresh");

        long start = System.currentTimeMillis();
        UtilTimerStack.push("ProjectCache.refresh");

        try
        {
            refreshProjectList();
            refreshProjectCategories();
            refreshCategoryProjectMappings();
            refreshProjectsWithNoCategory();
        }
        finally
        {
            UtilTimerStack.pop("ProjectCache.refresh");
        }

        if (log.isDebugEnabled())
            log.debug("ProjectCache.refresh took " + (System.currentTimeMillis() - start));
    }

    /**
     * Refresh the list of projects
     * <p/>
     * IMPACT: Should perform only one SQL select statement
     */
    private void refreshProjectList()
    {
        List<GenericValue> dbProjects = delegator.findAll("Project");
        Collections.sort(dbProjects, PROJECT_NAME_COMPARATOR);
        Map<Long, GenericValue> tmpById = new LinkedHashMap<Long, GenericValue>();
        Map<String, GenericValue> tmpByKey = new LinkedHashMap<String, GenericValue>();
        List<Project> tmpAllProjects = new ArrayList<Project>(dbProjects.size());
        for (GenericValue projectGV: dbProjects)
        {
            // Old-school GenericValue caches
            tmpById.put(projectGV.getLong("id"), projectGV);
            tmpByKey.put(projectGV.getString("key"), projectGV);
            // New School Project object cache
            tmpAllProjects.add(new ProjectImpl(projectGV));
        }

        // the caches are immutable, we recreate from scratch on refresh.
        projectsById = Collections.unmodifiableMap(tmpById);
        projectsByKey = Collections.unmodifiableMap(tmpByKey);
        allProjectObjects = Collections.unmodifiableList(tmpAllProjects);
    }

    protected void refreshProjectCategories()
    {
        List<GenericValue> dbCategories = delegator.findAll("ProjectCategory");
        Collections.sort(dbCategories, PROJECT_NAME_COMPARATOR);
        Map<Long, GenericValue> tmpById = new LinkedHashMap<Long, GenericValue>();
        for (final GenericValue projectCategory : dbCategories)
        {
            tmpById.put(projectCategory.getLong("id"), projectCategory);
        }

        projectCategories = Collections.unmodifiableMap(tmpById);
    }

    // PROJECT CACHING --------------------------------
    public GenericValue getProject(Long id)
    {
        return projectsById.get(id);
    }

    public GenericValue getProjectByName(String name)
    {
        for (final GenericValue project : getProjects())
        {
            if (project.getString("name").equalsIgnoreCase(name))
            {
                return project;
            }
        }

        return null;
    }

    public GenericValue getProjectByKey(String key)
    {
        return projectsByKey.get(key);
    }

    public Collection<GenericValue> getProjects()
    {
        return projectsById.values();
    }

    /**
     * Returns a list of all Projects ordered by name.
     * @return a list of all Projects ordered by name.
     */
    public List<Project> getProjectObjects()
    {
        return allProjectObjects;
    }

    public Collection<GenericValue> getProjectCategories()
    {
        return projectCategories.values();
    }

    public GenericValue getProjectCategory(Long id)
    {
        return projectCategories.get(id);
    }

    private void refreshCategoryProjectMappings()
    {
        Map<String, Long> tmpProjectToProjectCategories = new HashMap<String, Long>();
        Map<GenericValue, List<Long>> tmpProjectCategoriesToProjects = new HashMap<GenericValue, List<Long>>();

        final Collection<GenericValue> categories = getProjectCategories();

        if (categories != null)
        {
            for (final GenericValue category : categories)
            {
                try
                {
                    List<GenericValue> projects = ComponentAccessor.getComponentOfType(NodeAssociationStore.class).getSourcesFromSink(category, "Project", ProjectRelationConstants.PROJECT_CATEGORY);
                    Collections.sort(projects, PROJECT_NAME_COMPARATOR);

                    tmpProjectCategoriesToProjects.put(category, getIdsFromGenericValues(projects));
                    for (final GenericValue project : projects)
                    {
                        tmpProjectToProjectCategories.put(project.getString("key"), category.getLong("id"));
                    }
                }
                catch (DataAccessException ex)
                {
                    log.error("Error getting projects for category " + category + ": " + ex, ex); //TODO: What should this really do?
                }
            }
        }

        // Cached values are Immutable. We overwrite with a new cache on update.
        projectToProjectCategories = Collections.unmodifiableMap(tmpProjectToProjectCategories);
        projectCategoriesToProjects = Collections.unmodifiableMap(tmpProjectCategoriesToProjects);
    }

    private List<Long> getIdsFromGenericValues(Collection<GenericValue> genericValues)
    {
        if (genericValues == null)
            return Collections.EMPTY_LIST;

        List projectIds = new ArrayList(genericValues);

        CollectionUtils.transform(projectIds, new Transformer() {
            public Object transform(Object object)
            {
                if (object == null)
                    return null;
                else
                    return ((GenericValue) object).getLong("id");
            }
        });

        return projectIds;
    }

    private List getProjectsFromProjectIds(Collection projectIds)
    {
        if (projectIds == null)
            return Collections.EMPTY_LIST;

        List projects = new ArrayList(projectIds);

        CollectionUtils.transform(projects, new Transformer() {
            public Object transform(Object object)
            {
                if (object == null)
                    return null;
                else
                    return getProject((Long) object);
            }
        });

        return projects;

    }

    public Collection getProjectsFromProjectCategory(GenericValue projectCat)
    {
        if (projectCategoriesToProjects.containsKey(projectCat))
            return getProjectsFromProjectIds(projectCategoriesToProjects.get(projectCat));
        else
            return Collections.EMPTY_LIST;
    }

    public GenericValue getProjectCategoryForProject(Project project)
    {
        if (project != null && projectToProjectCategories.containsKey(project.getKey()))
        {
            Long projectCategoryId = projectToProjectCategories.get(project.getKey());
            return getProjectCategory(projectCategoryId);
        }
        else
        {
            return null;
        }
    }

    public GenericValue getProjectCategoryFromProject(GenericValue project)
    {
        if (project != null && projectToProjectCategories.containsKey(project.getString("key")))
        {
            Long projectId = projectToProjectCategories.get(project.getString("key"));
            return getProjectCategory(projectId);
        }
        else
        {
            return null;
        }
    }

    public Collection getProjectsWithNoCategory()
    {
        return getProjectsFromProjectIds(projectsWithNoCategory);
    }

    protected void refreshProjectsWithNoCategory()
    {
        projectsWithNoCategory = new ArrayList();
        List<GenericValue> projectsWithNoCategoryGVs = new ArrayList<GenericValue>();

        for (final GenericValue project : getProjects())
        {
            if (getProjectCategoryFromProject(project) == null)
            {
                projectsWithNoCategoryGVs.add(project);
            }
        }

        //alphabetic order on the project name
        Collections.sort(projectsWithNoCategoryGVs, PROJECT_NAME_COMPARATOR);
        projectsWithNoCategory = Collections.unmodifiableList(getIdsFromGenericValues(projectsWithNoCategoryGVs));
    }

}
