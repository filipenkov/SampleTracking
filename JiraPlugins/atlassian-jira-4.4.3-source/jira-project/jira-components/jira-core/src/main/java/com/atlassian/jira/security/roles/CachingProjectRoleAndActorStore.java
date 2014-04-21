package com.atlassian.jira.security.roles;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.util.OSUserConverter;
import com.google.common.base.Function;
import com.google.common.collect.MapMaker;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.atlassian.util.concurrent.Assertions.notNull;

/**
 * A caching implementation of the {@link ProjectRoleAndActorStore} that delegates to another {@link ProjectRoleAndActorStore}.
 * <p>
 * This class maintains two separate unrelated caches, one for ProjectRoles and another for the actors associated with a 
 * Project/ProjectRole combination. These use separate approaches to maintain correctness under concurrent usage.
 * <p>
 * The caching of the ProjectRoleActors maintains its correctness under concurrent updates/miss population by using
 * {@link ConcurrentMap#putIfAbsent(Object,Object)} to store the result of a retrieval operation from the database
 * (non-mutative), but {@link ConcurrentMap#put(Object,Object)} to store the result of an update.
 */
public class CachingProjectRoleAndActorStore implements ProjectRoleAndActorStore, Startable
{
    private final ProjectRoleAndActorStore delegate;
    private final RoleActorFactory roleActorFactory;
    private final EventPublisher eventPublisher;
    private final ProjectRoleActorsCache roleActorsCache = new ProjectRoleActorsCache();
    private final ProjectRoleCache roleCache = new ProjectRoleCache();

    public CachingProjectRoleAndActorStore(final ProjectRoleAndActorStore delegate, final RoleActorFactory roleActorFactory,
            final EventPublisher eventPublisher)
    {
        this.delegate = delegate;
        this.roleActorFactory = roleActorFactory;
        this.eventPublisher = eventPublisher;
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        clearCaches();
    }

    public ProjectRole addProjectRole(final ProjectRole projectRole) throws DataAccessException
    {
        final ProjectRole role = delegate.addProjectRole(projectRole);
        return roleCache.add(role);
    }

    public void updateProjectRole(final ProjectRole projectRole) throws DataAccessException
    {
        delegate.updateProjectRole(projectRole);
        roleCache.update(projectRole);
    }

    public Collection<ProjectRole> getAllProjectRoles() throws DataAccessException
    {
        return roleCache.getAll();
    }

    public ProjectRole getProjectRole(final Long id) throws DataAccessException
    {
        return roleCache.get(id);
    }

    public ProjectRole getProjectRoleByName(final String name) throws DataAccessException
    {
        return roleCache.get(name);
    }

    public void deleteProjectRole(final ProjectRole projectRole) throws DataAccessException
    {
        roleCache.remove(projectRole);
    }

    public DefaultRoleActors getDefaultRoleActors(final Long projectRoleId) throws DataAccessException
    {
        return roleActorsCache.get(projectRoleId, null);
    }

    public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId) throws DataAccessException
    {
        return (ProjectRoleActors) roleActorsCache.get(projectRoleId, projectId);
    }

    public void updateProjectRoleActors(final ProjectRoleActors projectRoleActors) throws DataAccessException
    {
        delegate.updateProjectRoleActors(projectRoleActors);
        // we MUST put the results in the cache rather than invalidating it so subsequent attempts to call putIfAbsent fail.
        roleActorsCache.put(projectRoleActors.getProjectId(), projectRoleActors);
    }

    public void updateDefaultRoleActors(final DefaultRoleActors defaultRoleActors) throws DataAccessException
    {
        delegate.updateDefaultRoleActors(defaultRoleActors);
        // we MUST put the results in the cache rather than invalidating it so subsequent attempts to call putIfAbsent fail.
        roleActorsCache.put(null, defaultRoleActors);
    }

    public void applyDefaultsRolesToProject(final Project project) throws DataAccessException
    {
        delegate.applyDefaultsRolesToProject(project);
    }

    public void removeAllRoleActorsByNameAndType(final String name, final String type) throws DataAccessException
    {
        delegate.removeAllRoleActorsByNameAndType(name, type);
        // Nuke the whole cache since we don't know which projects/roles this will effect
        roleActorsCache.clear();
    }

    public void removeAllRoleActorsByProject(final Project project) throws DataAccessException
    {
        delegate.removeAllRoleActorsByProject(project);
        roleActorsCache.removeByProject(project);
    }

    public Collection<Long> getProjectIdsContainingRoleActorByNameAndType(final String name, final String type) throws DataAccessException
    {
        return delegate.getProjectIdsContainingRoleActorByNameAndType(name, type);
    }

    public List<Long> roleActorOfTypeExistsForProjects(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String projectRoleParameter) throws DataAccessException
    {
        return delegate.roleActorOfTypeExistsForProjects(projectsToLimitBy, projectRole, projectRoleType, projectRoleParameter);
    }

    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String userName) throws DataAccessException
    {
        return delegate.getProjectIdsForUserInGroupsBecauseOfRole(projectsToLimitBy, projectRole, projectRoleType, userName);
    }

    public void clearCaches()
    {
        roleActorsCache.clear();
        roleCache.clear();
    }

    // --------------------------------------------------------------------------------------------------- inner doobies

    /**
     * This class maintains a Map<String, List<RoleTypeParameter>> that contains the RoleActors for a given Project and
     * ProjectRole combination. The key is a combination of "projectID:projectRoleID".
     */
    private class ProjectRoleActorsCache
    {
        private final ConcurrentMap<ProjectRoleActorKey, DefaultRoleActors> roleActorsByProjectAndRole = new MapMaker().makeComputingMap(
                new Function<ProjectRoleActorKey, DefaultRoleActors>()
                {
                    @Override
                    public DefaultRoleActors apply(@Nullable ProjectRoleActorKey from)
                    {
                        DefaultRoleActors roleActors;
                        if (from.getProjectId() == null)
                        {
                            roleActors = delegate.getDefaultRoleActors(from.getProjectRoleId());
                        }
                        else
                        {
                            roleActors = delegate.getProjectRoleActors(from.getProjectRoleId(), from.getProjectId());
                        }
                        if (roleActors != null)
                        {
                            return new CachedDefaultRoleActors(roleActors);
                        }
                        return null;
                    }
                }
        );

        void put(final Long projectId, final DefaultRoleActors roleActors)
        {
            roleActorsByProjectAndRole.put(new ProjectRoleActorKey(roleActors.getProjectRoleId(), projectId), new CachedDefaultRoleActors(roleActors));
        }

        DefaultRoleActors get(final Long projectRoleId, final Long projectId)
        {
            return roleActorsByProjectAndRole.get(new ProjectRoleActorKey(projectRoleId, projectId));
        }

        void remove(final Long projectRoleId, final Long projectId)
        {
            roleActorsByProjectAndRole.remove(new ProjectRoleActorKey(projectRoleId, projectId));
        }

        void removeByProject(final Project project)
        {
            for (final Iterator<ProjectRoleActorKey> iterator = roleActorsByProjectAndRole.keySet().iterator(); iterator.hasNext();)
            {
                final ProjectRoleActorKey key = iterator.next();
                if (key.getProjectId().equals(project.getId()))
                {
                    iterator.remove();
                }
            }
        }

        void clear()
        {
            roleActorsByProjectAndRole.clear();
        }

    }

    private class ProjectRoleActorKey
    {
        final Long projectRoleId;
        final Long projectId;

        private ProjectRoleActorKey(Long projectRoleId, Long projectId)
        {
            this.projectRoleId = projectRoleId;
            this.projectId = projectId;
        }

        public Long getProjectRoleId()
        {
            return projectRoleId;
        }

        public Long getProjectId()
        {
            return projectId;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) { return true; }
            if (o == null || getClass() != o.getClass()) { return false; }

            ProjectRoleActorKey that = (ProjectRoleActorKey) o;

            if (projectId != null ? !projectId.equals(that.projectId) : that.projectId != null) { return false; }
            if (projectRoleId != null ? !projectRoleId.equals(that.projectRoleId) : that.projectRoleId != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = projectRoleId != null ? projectRoleId.hashCode() : 0;
            result = 31 * result + (projectId != null ? projectId.hashCode() : 0);
            return result;
        }
    }

    /**
     * Encapsulates all the caching of ProjectRoles. This includes three caches, two that index on the name and id
     * properties of the ProjectRole, and one that simply holds all known ProjectRoles. This class does not attempt to
     * be smart about maintaining all indexes together, so if a name miss loads from the database, it doesn't load into
     * the id cache. Similarly getAll doesn't load either of the other caches.
     * <p/>
     * A {@link ReadWriteLock} is used to maintain atomic updates to this cache. This is possibly unnecessary as each
     * cache should work in isolation, but is done to guarantee Anton's peace of mind.
     */
    private class ProjectRoleCache
    {
        private final ReadWriteLock lock = new ReentrantReadWriteLock();
        private final List<ProjectRole> projectRoles = new CopyOnWriteArrayList<ProjectRole>();

        private final ProjectRoleMap projectRolesByName = new ProjectRoleMap()
        {
            @Override
            String getKey(final ProjectRole role)
            {
                return role.getName();
            }

            @Override
            ProjectRole getFromDelegate(final String key)
            {
                return delegate.getProjectRoleByName(key);
            }
        };

        private final ProjectRoleMap projectRolesById = new ProjectRoleMap()
        {
            @Override
            String getKey(final ProjectRole role)
            {
                return role.getId().toString();
            }

            @Override
            ProjectRole getFromDelegate(final String key)
            {
                return delegate.getProjectRole(new Long(key));
            }
        };

        void clear()
        {
            lock.writeLock().lock();
            try
            {
                projectRoles.clear();
                projectRolesById.clear();
                projectRolesByName.clear();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        ProjectRole add(final ProjectRole role)
        {
            lock.writeLock().lock();
            try
            {
                projectRolesById.add(role);
                projectRolesByName.add(role);
                projectRoles.clear();
                return role;
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        void update(final ProjectRole projectRole) throws DataAccessException
        {
            lock.writeLock().lock();
            try
            {
                final ProjectRole oldRole = projectRolesById.remove(projectRole);
                projectRolesByName.remove(projectRole);
                if (oldRole != null)
                {
                    projectRolesByName.remove(oldRole);
                }
                projectRoles.clear();
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }

        ProjectRole get(final String name)
        {
            lock.readLock().lock();
            try
            {
                return projectRolesByName.get(name);
            }
            finally
            {
                lock.readLock().unlock();
            }
        }

        ProjectRole get(final Long id)
        {
            lock.readLock().lock();
            try
            {
                return projectRolesById.get(id.toString());
            }
            finally
            {
                lock.readLock().unlock();
            }
        }

        Collection<ProjectRole> getAll() throws DataAccessException
        {
            // common path is that the collection is populated, therefore don't get a write lock as we don't intend to write
            if (projectRoles.size() == 0)
            {
                final Collection<ProjectRole> allProjectRoles = delegate.getAllProjectRoles();
                lock.writeLock().lock();
                try
                {
                    // make sure noone has updated it in the meantime
                    if (projectRoles.size() == 0)
                    {
                        projectRoles.addAll(allProjectRoles);
                    }
                }
                finally
                {
                    lock.writeLock().unlock();
                }
            }
            // don't forget we have a CopyOnWriteArrayList so we don't need to duplicate it to protect against ConcurrentMod
            return Collections.unmodifiableCollection(projectRoles);
        }

        public void remove(final ProjectRole projectRole)
        {
            delegate.deleteProjectRole(projectRole);
            if (projectRole == null)
            {
                return;
            }
            lock.writeLock().lock();
            try
            {
                ProjectRole oldRole = null;
                if (projectRole.getId() != null)
                {
                    oldRole = projectRolesById.remove(projectRole);
                }
                if (projectRole.getName() != null)
                {
                    projectRolesByName.remove(projectRole);
                }
                if (oldRole != null)
                {
                    // remove the old one whose name has changed
                    projectRolesByName.remove(projectRole);
                }
                projectRoles.remove(projectRole);
            }
            finally
            {
                lock.writeLock().unlock();
            }
        }
    }

    /**
     * This is a Map wrapper that gives us static typing and the ability to reuse the logic for both Name and ID
     * indexes. It also handles cache misses by calling through to the delegate.
     */
    private abstract class ProjectRoleMap
    {
        private final ConcurrentMap<String, ProjectRole> map = new ConcurrentHashMap<String, ProjectRole>();

        ProjectRole get(final String key)
        {
            ProjectRole projectRole = map.get(key);
            if (projectRole == null)
            {
                projectRole = getFromDelegate(key);
                if (projectRole != null)
                {
                    // JRA-13157 - we want to cache the role retrieved by the name in the DB value, not the value that
                    // was passed it, in this way we will not put case-insensitive matches into the cache, these will
                    // always miss and go to the DB, BUT will be correct.
                    final ProjectRole result = map.putIfAbsent(getKey(projectRole), projectRole);
                    return (result == null) ? projectRole : result;
                }
            }
            return projectRole;
        }

        void add(final ProjectRole role)
        {
            notNull("ProjectRole cannot be null", role);
            map.put(getKey(role), role);
        }

        ProjectRole remove(final ProjectRole role)
        {
            notNull("ProjectRole cannot be null", role);
            return map.remove(getKey(role));
        }

        void clear()
        {
            map.clear();
        }

        abstract String getKey(ProjectRole role);

        abstract ProjectRole getFromDelegate(String key);
    }

    /**
     * CachedProjectRoleActors contains an optimized contains(user) method.
     */
    private class CachedDefaultRoleActors implements ProjectRoleActors
    {
        private final DefaultRoleActors delegate;
        private final Set<RoleActor> optimizedProjectRoleSet;

        CachedDefaultRoleActors(final DefaultRoleActors delegate)
        {
            this.delegate = delegate;
            optimizedProjectRoleSet = Collections.unmodifiableSet(roleActorFactory.optimizeRoleActorSet(delegate.getRoleActors()));
        }

        /*
         * The optimized set of RoleActor instances is used.
         */
        public boolean contains(final com.opensymphony.user.User user)
        {
            for (final RoleActor o : optimizedProjectRoleSet)
            {
                if (o.contains(user))
                {
                    return true;
                }
            }
            return false;
        }

        public boolean contains(final User user)
        {
            return contains(OSUserConverter.convertToOSUser(user));
        }

        public Long getProjectId()
        {
            return (delegate instanceof ProjectRoleActors) ? ((ProjectRoleActors) delegate).getProjectId() : null;
        }

        public Set<com.opensymphony.user.User> getUsers()
        {
            return delegate.getUsers();
        }

        public Set<RoleActor> getRoleActors()
        {
            return delegate.getRoleActors();
        }

        public Long getProjectRoleId()
        {
            return delegate.getProjectRoleId();
        }

        public Set<RoleActor> getRoleActorsByType(final String type)
        {
            return delegate.getRoleActorsByType(type);
        }

        public DefaultRoleActors addRoleActors(final Collection<RoleActor> roleActors)
        {
            return delegate.addRoleActors(roleActors);
        }

        public DefaultRoleActors addRoleActor(final RoleActor roleActor)
        {
            return delegate.addRoleActor(roleActor);
        }

        public DefaultRoleActors removeRoleActor(final RoleActor roleActor)
        {
            return delegate.removeRoleActor(roleActor);
        }

        public DefaultRoleActors removeRoleActors(final Collection<RoleActor> roleActors)
        {
            return delegate.removeRoleActors(roleActors);
        }
    }
}
