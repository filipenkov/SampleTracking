package com.atlassian.jira.bc.project.component;

import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.bc.EntityNotFoundException;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.util.dbc.Assertions;
import com.opensymphony.util.TextUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Decorates an implementation of the project component delegateStore with caching. The actual delegateStore
 * implementation is delegated so this class is a Composite and also a Decorator.
 */
@EventComponent
public class CachingProjectComponentStore implements ProjectComponentStore
{
    private static final Logger log = Logger.getLogger(CachingProjectComponentStore.class);

    /**
     * component ID -> component. (Map&lt;Long, MutableProjectComponent&gt;)
     */
    private Map<Long, MutableProjectComponent> componentIdToComponentMap = new ConcurrentHashMap<Long, MutableProjectComponent>();

    /**
     * project ID -> list of component names. (Map&lt;Long, List&lt;String&gt;&gt;)
     */
    private Map<Long, List<String>> projectIdToComponentNamesMap = new ConcurrentHashMap<Long, List<String>>();

    /**
     * project ID -> list of components. (Map&lt;Long, List&lt;MutableProjectComponent&gt;&gt;)
     */
    private Map<Long, List<MutableProjectComponent>> projectIdToComponentsMap = new ConcurrentHashMap<Long, List<MutableProjectComponent>>();

    /**
     * component ID -> project ID. (Map&lt;Long, Long&gt;)
     */
    private Map<Long, Long> componentIdToProjectIdMap = new ConcurrentHashMap<Long, Long>();

    /**
     * backing delegateStore
     */
    private final ProjectComponentStore delegateStore;

    /**
     * Creates a new instance of this class backed by given delegateStore.
     * Initialises the cache with the data in the persistence store.
     *
     * @param delegateStore underlying persistence store
     *
     */
    public CachingProjectComponentStore(final ProjectComponentStore delegateStore)
    {
        this.delegateStore = delegateStore;
        initCache();
    }

    @EventListener
    public void onClearCache(ClearCacheEvent event)
    {
        componentIdToProjectIdMap.clear();
        projectIdToComponentsMap.clear();
        projectIdToComponentNamesMap.clear();
        componentIdToComponentMap.clear();
        // Re-init the cache
        initCache();
    }

    /**
     * Looks up the project component by the given ID and returns it. If not found, throws the EntityNotFoundException,
     * it never returns null.
     *
     * @param id project component ID
     * @return project component found by a given ID
     * @throws EntityNotFoundException if the component not found
     */
    public MutableProjectComponent find(Long id) throws EntityNotFoundException
    {
        Assertions.notNull("id", id);

        MutableProjectComponent component = componentIdToComponentMap.get(id);
        if (component == null)
        {
            throw new EntityNotFoundException("The component with id '" + id + "' does not exist.");
        }
        // TODO: Why do we cache Mutable objects, only to have to clone them all the time (here and in the Manager)? we should cache Immutable objects.
        return MutableProjectComponent.copy(component);
    }

    /**
     * Looks up all components that are related to the project with given ID.
     *
     * @param projectId project ID
     * @return a collection of ProjectComponent objects that are related to the project with given ID
     */
    public synchronized Collection findAllForProject(Long projectId)
    {
        Collection components = projectIdToComponentsMap.get(projectId);
        return MutableProjectComponent.copy(components);
    }


    /**
     * Looks up the component with the given name in the project with the given id.
     *
     * Not synchronised, because we get a private copy of the list from findAllForProject()
     *
     * @param projectId id of the project.
     * @param componentName name of the component.
     * @return the component.
     * @throws EntityNotFoundException if no such component can be found.
     */
    public MutableProjectComponent findByComponentName(Long projectId, String componentName)
            throws EntityNotFoundException
    {
        Collection components = findAllForProject(projectId);
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            MutableProjectComponent c = (MutableProjectComponent) iterator.next();
            if (c.getName().equals(componentName))
            {
                return c;
            }
        }
        throw new EntityNotFoundException("The project with id '" + projectId + "' is not associated with a component with the name '" + componentName + "'.");
    }

    /**
     * Finds one or more ProjectComponent with a given name.
     *
     * Not synchronised, because findAll() returns a private copy of all components.
     *
     * @param componentName the name of the component to find.
     * @return a Collection of Components with the given name.
     * @throws EntityNotFoundException
     */
    public Collection<MutableProjectComponent> findByComponentNameCaseInSensitive(String componentName)
    {
        Collection<MutableProjectComponent> components = findAll();
        Collection<MutableProjectComponent> matchingComponents = new ArrayList<MutableProjectComponent>();

        for (final MutableProjectComponent component : components)
        {
            if (component.getName().equalsIgnoreCase(componentName))
            {
                matchingComponents.add(component);
            }
        }
        return matchingComponents;
    }

    /**
     * Looks up the project ID for the given component ID. If project is not found, throws EntityNotFoundException.
     *
     * @param componentId component ID
     * @return project ID
     * @throws EntityNotFoundException if project not found for the given component ID
     */
    public Long findProjectIdForComponent(Long componentId) throws EntityNotFoundException
    {
        Long projectId = componentIdToProjectIdMap.get(componentId);
        if (projectId == null)
        {
            throw new EntityNotFoundException("The component with the id '" + componentId + "' does not exist.");
        }
        return projectId;
    }

    /**
     * Checks whether component with specified name is stored.
     *
     * @param name component name, null will cause IllegalArgumentException
     * @return true if new name is stored
     * @throws IllegalArgumentException if name or projectId is null
     */
    public synchronized boolean containsName(String name, Long projectId)
    {
        if (projectId == null)
        {
            throw new IllegalArgumentException("Component project ID can not be null!");
        }
        if (name == null)
        {
            throw new IllegalArgumentException("Component name can not be null!");
        }
        Collection names = projectIdToComponentNamesMap.get(projectId);
        return containsNameIgnoreCase(names, name);
    }

    static boolean containsNameIgnoreCase(Collection names, String name)
    {
        boolean containsName = false;
        if (names != null)
        {
            for (Iterator iterator = names.iterator(); iterator.hasNext();)
            {
                String componentName = (String) iterator.next();
                if (name.equalsIgnoreCase(componentName))
                {
                    containsName = true;
                }
            }
        }
        return containsName;
    }

    /**
     * Persist the component. If component has no ID (null) it is inserted to the database and added to the cache,
     * otherwise an update operation is performed on both cache and database. Note: this method doesn't need to be
     * synchronized because it just delegates to others that are.
     *
     * @param component component to persist
     * @throws EntityNotFoundException in case of update if the component does not exist (maybe was deleted :-)
     * @throws com.atlassian.jira.exception.DataAccessException if cannot persist the component
     */
    public synchronized MutableProjectComponent store(MutableProjectComponent component)
            throws EntityNotFoundException, DataAccessException
    {
        MutableProjectComponent copy = MutableProjectComponent.copy(component);
        MutableProjectComponent newComponent;
        if (copy.getId() == null)
        {
            newComponent = insert(copy);
        }
        else
        {
            newComponent = update(copy);
        }
        sortByComponentNames();
        return newComponent;
    }

    /**
     * Removes the component from the persistent storage and a cache.
     *
     * @param componentId the id of the component to delete
     * @throws EntityNotFoundException if component does not exist (maybe was removed previously :-)
     */
    public synchronized void delete(Long componentId) throws EntityNotFoundException
    {
        delegateStore.delete(componentId);
        deleteFromCache(componentId);
        sortByComponentNames();
    }

    /**
     * Retrieves all ProjectComponents that have the given user as their lead.
     * Not synchronised, because findAll() returns a private copy of all components.
     *
     * @param userName user name
     * @return possibly empty Collection of ProjectComponents.
     */
    public Collection findComponentsBylead(String userName)
    {
        Collection leadComponents = new ArrayList();
        Collection components = findAll();
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            MutableProjectComponent projectComponent = (MutableProjectComponent) iterator.next();
            if (projectComponent != null && TextUtils.stringSet(projectComponent.getLead()) && projectComponent.getLead().equals(userName))
            {
                leadComponents.add(projectComponent);
            }

        }
        return leadComponents;
    }

    /**
     * Retrieve all ProjectComponent objects stored.
     *
     * @return all ProjectComponent objects stored
     */
    public Collection findAll()
    {
        return new ArrayList(componentIdToComponentMap.values());
    }

    // helper methods

    /**
     * Add the specified component to the persistent storage and the cache. The returned component has ID set -
     * indicating that it has been persisted.
     *
     * @param component the component to stored
     * @return the updated component (with ID set)
     */
    private synchronized MutableProjectComponent insert(MutableProjectComponent component)
    {
        String name = component.getName();
        if (containsName(name, component.getProjectId()))
        {
            throw createIllegalArgumentExceptionForName(name);
        }

        try
        {
            component = delegateStore.store(component);
            addToCache(component);
            return component;
        }
        catch (EntityNotFoundException e)
        {
            // This exception should never be thrown - insertion should always complete successfully
            return null;
        }
    }

    /**
     * Retrieve the component with the ID of the component specified and update it with the new values of the given
     * component.
     *
     * @param component component with new values
     * @return updated component
     * @throws EntityNotFoundException if component with ID does not exist
     * @throws com.atlassian.jira.exception.DataAccessException if cannot persist the component
     * @throws IllegalArgumentException if duplicate name
     */
    private synchronized MutableProjectComponent update(MutableProjectComponent component)
            throws EntityNotFoundException, DataAccessException
    {
        MutableProjectComponent old = find(component.getId());
        if (!old.equalsName(component))
        {
            if (containsName(component.getName(), component.getProjectId()))
            {
                throw new IllegalArgumentException("New component name '" + component.getName() + "' is not unique!");
            }
        }
        delegateStore.store(component);
        updateCache(component);
        return component;
    }

    /**
     * Add the given component to the cache
     *
     * @param component component to add to cache
     * @throws IllegalArgumentException if the component id is null or the component project id is null.
     * ConcurrentHashMap does not allow null keys or values.
     */
    private synchronized void addToCache(MutableProjectComponent component)
    {
        if (component.getId() == null)
        {
            throw new IllegalArgumentException("Component Id cannot be null!");
        }
        if (component.getProjectId() == null)
        {
            throw new IllegalArgumentException("Component Project Id cannot be null!");
        }

        component = MutableProjectComponent.copy(component);
        componentIdToComponentMap.put(component.getId(), component);

        if (!projectIdToComponentNamesMap.containsKey(component.getProjectId()))
        {
            projectIdToComponentNamesMap.put(component.getProjectId(), new ArrayList<String>());
        }
        projectIdToComponentNamesMap.get(component.getProjectId()).add(component.getName());

        if (!projectIdToComponentsMap.containsKey(component.getProjectId()))
        {
            projectIdToComponentsMap.put(component.getProjectId(), new ArrayList<MutableProjectComponent>());
        }
        projectIdToComponentsMap.get(component.getProjectId()).add(component);
        componentIdToProjectIdMap.put(component.getId(), component.getProjectId());
    }

    /**
     * Update the cache with the given component Project ID and component ID changes are ignored - they shouldn't
     * change.
     *
     * @param component component to be updated in cache
     */
    private synchronized void updateCache(MutableProjectComponent component)
    {
        Long id = component.getId();
        MutableProjectComponent cachedComponent = componentIdToComponentMap.get(id);

        // This exact reference is in the cache maps. We don't need to update the projectIdToComponentsMap directly,
        // only properties of the already cached component.

        String newName = component.getName();
        String oldName = cachedComponent.getName();
        Long projectId = component.getProjectId();
        projectIdToComponentNamesMap.get(projectId).add(newName);
        projectIdToComponentNamesMap.get(projectId).remove(oldName);

        // update the cached object
        //  - ID and project ID is not going to change
        cachedComponent.setName(newName);
        cachedComponent.setDescription(component.getDescription());
        cachedComponent.setLead(component.getLead());
        cachedComponent.setAssigneeType(component.getAssigneeType());

    }

    /**
     * Delete the specified component from the cache. If all components of a project have been deleted, we delete the
     * entry.
     *
     * @param componentId Id of component to delete
     */
    private synchronized void deleteFromCache(Long componentId)
    {
        MutableProjectComponent projectComponent = componentIdToComponentMap.get(componentId);
        componentIdToComponentMap.remove(componentId);

        final List<String> componentNamesList = projectIdToComponentNamesMap.get(projectComponent.getProjectId());
        if (componentNamesList == null)
        {
            log.warn("Project component names cache for project '" + projectComponent.getProjectId() + "' is null, when trying to delete component: '" + componentId + "'");
        }else
        {
            componentNamesList.remove(projectComponent.getName());
        }

        final List<MutableProjectComponent> componentsList = projectIdToComponentsMap.get(projectComponent.getProjectId());
        if (componentsList == null)
        {
            log.warn("Project component object cache for project '" + projectComponent.getProjectId() + "' is null, when trying to delete component: '" + componentId + "'");
        }else
        {
            componentsList.remove(projectComponent);
        }
        componentIdToProjectIdMap.remove(componentId);
    }


    private IllegalArgumentException createIllegalArgumentExceptionForName(String name)
    {
        return new IllegalArgumentException("Component name = '" + name + "' is not unique");
    }

    /**
     * Sorts the List of {@link MutableProjectComponent} in projectIdToComponentsMap using the
     * ProjectComponentComparator.
     * When returning components for a project the components have to be sorted, due to compatiblity with the
     * deprecated ProjectManager.
     *
     * Call is synchronized to avoid conflicts with other mutant operations.
     */
    private synchronized void sortByComponentNames()
    {
        for (Iterator<Long> iterator = projectIdToComponentsMap.keySet().iterator(); iterator.hasNext();)
        {
            final Long projectId = iterator.next();
            final List<MutableProjectComponent> componentList = projectIdToComponentsMap.get(projectId);
            if (componentList != null)
            {
                Collections.sort(componentList, ProjectComponentComparator.INSTANCE);
            }
            else
            {
                log.warn("Project component object cache for project '" + projectId + "' is null, when trying to sort components by name.'");
            }
        }
    }

    /**
     * Initialise the cache with the data in the persistent store.
     */
    private void initCache()
    {
        Collection components = delegateStore.findAll();
        for (Iterator iterator = components.iterator(); iterator.hasNext();)
        {
            MutableProjectComponent projectComponent = (MutableProjectComponent) iterator.next();
            addToCache(projectComponent);
        }
        sortByComponentNames();
    }
}
