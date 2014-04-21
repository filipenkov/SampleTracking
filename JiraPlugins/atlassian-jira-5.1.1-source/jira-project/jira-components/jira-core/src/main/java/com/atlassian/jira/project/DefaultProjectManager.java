/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.Update;
import com.atlassian.jira.event.ProjectCreatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.LockManager;
import com.atlassian.util.concurrent.LockManagers;
import com.google.common.collect.MapMaker;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * A class to manage interactions with projects
 */
public class DefaultProjectManager extends AbstractProjectManager
{
    private static final Logger log = Logger.getLogger(DefaultProjectManager.class);

    private static final String PROJECT_ENTITY_NAME = "Project";

    private final OfBizDelegator delegator;
    private final NodeAssociationStore nodeAssociationStore;
    private final ProjectFactory projectFactory;
    private final ProjectRoleManager projectRoleManager;
    private final IssueManager issueManager;
    private final AvatarManager avatarManager;
    private final ProjectCategoryStore projectCategoryStore;
    private final EventPublisher eventPublisher;


    private final NextIdGenerator nextIdGenerator;

    /**
     * Don't use this constructor.
     * <p/>
     * @deprecated Use Pico instead. Since 2002.
     */
    public DefaultProjectManager()
    {
        this(ComponentAccessor.getOfBizDelegator(), ComponentAccessor.getComponentOfType(NodeAssociationStore.class),
            ComponentManager.getComponentInstanceOfType(ProjectFactory.class), ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class),
            ComponentManager.getComponentInstanceOfType(IssueManager.class),
                ComponentManager.getComponentInstanceOfType(AvatarManager.class), ComponentManager.getComponentInstanceOfType(UserManager.class),
            ComponentAccessor.getComponentOfType(ProjectCategoryStore.class), ComponentAccessor.getApplicationProperties(), ComponentAccessor.getComponentOfType(EventPublisher.class));
    }

    public DefaultProjectManager(final OfBizDelegator delegator, final NodeAssociationStore nodeAssociationStore, final ProjectFactory projectFactory, final ProjectRoleManager projectRoleManager, final IssueManager issueManager, AvatarManager avatarManager, UserManager userManager,
        ProjectCategoryStore projectCategoryStore, ApplicationProperties applicationProperties, EventPublisher eventPublisher)
    {
        super(userManager, applicationProperties);
        this.delegator = delegator;
        this.nodeAssociationStore = nodeAssociationStore;
        this.projectFactory = projectFactory;
        this.projectRoleManager = projectRoleManager;
        this.issueManager = issueManager;
        this.avatarManager = avatarManager;
        this.projectCategoryStore = projectCategoryStore;
        this.eventPublisher = eventPublisher;
        this.nextIdGenerator = new NextIdGenerator(this, delegator);
    }

    @Override
    public Project createProject(final String name, final String key, final String description, final String lead, final String url, final Long assigneeType, Long avatarId)
    {
        notNull("key", key);
        notNull("name", name);
        notNull("lead", lead);
        if (avatarId == null)
        {
            avatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
        }
        final Map<String, Object> params = new PrimitiveMap.Builder().add("key", key).add("name", name).add("url", url).add("lead", lead).add(
            "description", description).add("counter", 0L).add("assigneetype", assigneeType).add("avatar", avatarId).toMap();
        final GenericValue projectGV = delegator.createValue(PROJECT_ENTITY_NAME, params);

        final Project newProject = new ProjectImpl(projectGV);

        // Give the project role manager a chance to assign the project role default actors to this new project
        projectRoleManager.applyDefaultsRolesToProject(newProject);
        nextIdGenerator.newProject(newProject.getId());
        eventPublisher.publish(new ProjectCreatedEvent(newProject.getId()));
        return newProject;
    }

    // Create Methods --------------------------------------------------------------------------------------------------

    @Override
    public long getNextId(final Project project)
    {
        return nextIdGenerator.getNextId(project);
    }

    /**
     * This is a sanity check to ensure that we are only giving out project keys that haven't already been given out.
     * <p> In an ideal world, this should never return true. </p> Note that this method isn't guaranteed to avoid
     * duplicates, as it will only work if the Issue has already been inserted in the DB.  However the pkey column
     * has had a duplicate constraint added, in the event that you get through this.
     *
     * @param project The project
     * @param counter the suggested Issue number
     *
     * @return true if this Issue Key already exists in the DB.
     */
    private boolean counterAlreadyExists(final Project project, final long counter)
    {
        final String issueKey = project.getKey() + "-" + counter;
        final List<GenericValue> result = getDelegator().findByAnd("Issue", FieldMap.build("key", issueKey));
        final boolean alreadyExists = !result.isEmpty();
        if (alreadyExists)
        {
            log.warn("Existing issue found for key " + issueKey + ". Incrementing key.");
        }
        return (alreadyExists);
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String lead, final String url, final Long assigneeType, Long avatarId)
    {
        notNull("project", updatedProject);
        notNull("name", name);
        notNull("lead", lead);

        // Make a fresh Project GV and only add a subset of fields because we don't want to overwrite "counter"
        final GenericValue projectUpdate = delegator.makeValue("Project");

        projectUpdate.set("id", updatedProject.getId());
        projectUpdate.setString("name", name);
        projectUpdate.setString("url", url);
        projectUpdate.setString("lead", lead);
        projectUpdate.setString("description", description);
        projectUpdate.set("assigneetype", assigneeType);
        if (avatarId != null)
        {
            projectUpdate.set("avatar", avatarId);
        }

        // Store the partial update
        delegator.store(projectUpdate);

        // JRA-18152: must clear the issue security level cache so that if project lead has changed, user permissions are recalculated
        getIssueSecurityLevelManager().clearUsersLevels();

        return getProjectObj(updatedProject.getId());
    }

    @Override
    public void removeProjectIssues(final Project project) throws RemoveException
    {
        notNull("project", project);

        final Collection<Long> issueIds;
        try
        {
            issueIds = issueManager.getIssueIdsForProject(project.getId());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        for (final Long issueId : issueIds)
        {
            final Issue issue = issueManager.getIssueObject(issueId);
            // We have retrieved all issue ids for the project.
            if (issue != null)
            {
                try
                {
                    issueManager.deleteIssueNoEvent(issue);
                }
                catch (final Exception e)
                {
                    log.error("Exception removing issues", e);
                    throw new RemoveException("Error removing issues: " + e, e);
                }
            }
            else
            {
                log.debug("Issue with id '" + issueId + "' was not found. Most likely it is a sub-task and has been deleted previously with its parent.");
            }
        }
    }

    @Override
    public void removeProject(final Project project)
    {
        notNull("project", project);

        // Remove all project role associations for this project from the projectRoleManager
        projectRoleManager.removeAllRoleActorsByProject(project);

        // remove the project itself
        Delete.from("Project")
                .whereIdEquals(project.getId())
                .execute(delegator);
    }

    @Override
    public GenericValue getProject(final Long id)
    {
        return getDelegator().findById("Project", id);
    }

    @Override
    public Project getProjectObj(final Long id)
    {
        Project project = null;
        final GenericValue gv = getProject(id);
        if (gv != null)
        {
            project = projectFactory.getProject(gv);
        }
        return project;
    }

    @Override
    public GenericValue getProjectByName(final String name)
    {
        return EntityUtil.getOnly(getDelegator().findByAnd("Project", FieldMap.build("name", name)));
    }

    @Override
    public GenericValue getProjectByKey(final String key)
    {
        return EntityUtil.getOnly(getDelegator().findByAnd("Project", FieldMap.build("key", key)));
    }

    @Override
    public Project getProjectObjByKey(final String projectKey)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByKey(projectKey);
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
    public Project getProjectObjByName(final String projectName)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByName(projectName);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public GenericValue getComponent(final Long id)
    {
        return getDelegator().findById("Component", id);
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public GenericValue getComponent(final GenericValue project, final String name)
    {
        // don't bother caching - only used when creating components anyway
        return EntityUtil.getOnly(getDelegator().findByAnd("Component", FieldMap.build("project", project.getLong("id"), "name", name)));
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public Collection<GenericValue> getComponents(final GenericValue project)
    {
        try
        {
            return project.getRelated("ChildComponent",
                    Collections.<String, Object>emptyMap(), Collections.singletonList("name"));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    /**
     * Return all project {@link GenericValue}s.
     */
    @Override
    public Collection<GenericValue> getProjects()
    {
        final List<GenericValue> allProjects = getDelegator().findAll("Project", CollectionBuilder.list("name"));
        Collections.sort(allProjects, OfBizComparators.NAME_COMPARATOR); //Fixes JRA-1246
        return allProjects;
    }

    @Override
    public List<Project> getProjectObjects() throws DataAccessException
    {
        final Collection<GenericValue> projectGVs = getProjects();
        return projectFactory.getProjects(projectGVs);
    }

    // Business Logic Methods ------------------------------------------------------------------------------------------

    protected OfBizDelegator getDelegator()
    {
        return delegator;
    }

    @Override
    public Collection<GenericValue> getProjectCategories()
    {
        return getDelegator().findAll("ProjectCategory", EasyList.build("name"));
    }

    @Override
    public List<ProjectCategory> getAllProjectCategories()
    {
        return projectCategoryStore.getAllProjectCategories();
    }

    @Override
    public GenericValue getProjectCategory(final Long id)
    {
        return getDelegator().findByPrimaryKey("ProjectCategory", id);
    }

    @Override
    public ProjectCategory getProjectCategoryObject(final Long id)
    {
        return projectCategoryStore.getProjectCategory(id);
    }

    @Override
    public void updateProjectCategory(final GenericValue projectCat)
    {
        getDelegator().storeAll(CollectionBuilder.newBuilder(projectCat).asList());
    }

    @Override
    public void updateProjectCategory(ProjectCategory projectCategory) throws DataAccessException
    {
        projectCategoryStore.updateProjectCategory(projectCategory);
    }

    /**
     * Gather a list of projects that are in a project category.
     *
     * @param projectCategory Project to look up against
     * @return Collection of Projects
     */
    @Override
    public Collection<GenericValue> getProjectsFromProjectCategory(final GenericValue projectCategory)
    {
        if (null == projectCategory)
        {
            return Collections.emptyList();
        }

        final List<GenericValue> projects = nodeAssociationStore.getSourcesFromSink(projectCategory, "Project",
            ProjectRelationConstants.PROJECT_CATEGORY);

        //alphabetic order on the project name
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
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

    /**
     * Gets a list of projects that are not associated with any project category
     */
    @Override
    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        final List<GenericValue> result = new ArrayList<GenericValue>();
        for (final GenericValue project : getProjects())
        {
            if (getProjectCategoryFromProject(project) == null)
            {
                result.add(project);
            }
        }

        //alphabetic order on the project name
        Collections.sort(result, OfBizComparators.NAME_COMPARATOR);
        return result;
    }

    @Override
    public Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException
    {
        return projectFactory.getProjects(getProjectsWithNoCategory());
    }

    /**
     * Get the Project Category  given a Project.
     *
     * @param project Project
     * @return Project Category
     */
    @Override
    public GenericValue getProjectCategoryFromProject(final GenericValue project)
    {
        if (null == project)
        {
            return null;
        }

        final List<GenericValue> projectCats = nodeAssociationStore.getSinksFromSource(project, "ProjectCategory",
            ProjectRelationConstants.PROJECT_CATEGORY);

        if ((null == projectCats) || projectCats.isEmpty())
        {
            return null;
        }

        return projectCats.iterator().next();
    }

    @Override
    public ProjectCategory getProjectCategoryForProject(Project project) throws DataAccessException
    {
        if (project == null)
        {
            return null;
        }

        final List<GenericValue> projectCats = nodeAssociationStore.getSinksFromSource(PROJECT_ENTITY_NAME, project.getId(), "ProjectCategory",
            ProjectRelationConstants.PROJECT_CATEGORY);

        if ((null == projectCats) || projectCats.isEmpty())
        {
            return null;
        }

        return Entity.PROJECT_CATEGORY.build(projectCats.get(0));
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        return projectCategoryStore.createProjectCategory(name, description);
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        projectCategoryStore.removeProjectCategory(id);
    }

    /**
     * If <code>category</code> is non-null, set <code>project</code>'s Project Category to <code>category</code>. If
     * <code>category</code> is null, remove <code>project</code>'s Project Category association, if one exists.
     */
    @Override
    public void setProjectCategory(final GenericValue project, final GenericValue category)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Cannot associate a category with a null project");
        }

        final GenericValue oldProjectCategory = getProjectCategoryFromProject(project);

        if (null != oldProjectCategory)
        {
            nodeAssociationStore.removeAssociation(project, oldProjectCategory, ProjectRelationConstants.PROJECT_CATEGORY);
        }

        if (null != category)
        {
            nodeAssociationStore.createAssociation(project, category, ProjectRelationConstants.PROJECT_CATEGORY);
        }
    }

    @Override
    public void setProjectCategory(Project project, ProjectCategory category)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Cannot associate a category with a null project");
        }

        final ProjectCategory oldProjectCategory = getProjectCategoryForProject(project);

        if (oldProjectCategory != null)
        {
            nodeAssociationStore.removeAssociation(ProjectRelationConstants.PROJECT_CATEGORY_ASSOC, project.getId(), oldProjectCategory.getId());
        }

        if (category != null)
        {
            nodeAssociationStore.createAssociation(ProjectRelationConstants.PROJECT_CATEGORY_ASSOC, project.getId(), category.getId());
        }
    }

    @Override
    public List<Project> getProjectsLeadBy(User leadUser)
    {
        List<GenericValue> projects = findProjectsByLead(leadUser);
        return projectFactory.getProjects(projects);
    }

    @Override
    public final Collection<GenericValue> getProjectsByLead(final User leadUser)
    {
        return findProjectsByLead(leadUser);
    }

    private List<GenericValue> findProjectsByLead(final User leadUser)
    {
        if (leadUser == null)
        {
            return Collections.emptyList();
        }
        // ordering by name of project
        return getDelegator().findByAnd("Project", FieldMap.build("lead", leadUser.getName()), CollectionBuilder.list("name"));
    }

    @Override
    public void refresh()
    {
        nextIdGenerator.refresh();
    }

    ///CLOVER:OFF
    IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return ComponentManager.getComponentInstanceOfType(IssueSecurityLevelManager.class);
    }
    ///CLOVER:ON


    @Override
    public long getCurrentCounterForProject(Long id)
    {
        return nextIdGenerator.getCurrentCounterForProject(id);
    }

    @Override
    public void setCurrentCounterForProject(Project project, long counter)
    {
        nextIdGenerator.resetCounter(project, counter);
    }

    /**
     * Responsible for generating the next Issue Key.
     */
    static class NextIdGenerator
    {
        private final OfBizDelegator ofBizDelegator;
        private final ProjectCounterCache projectCounterCache;

        // try to limit starvation possibilities - 4 maximum connections for updating project counter
        private final ExecutorService projectCountUpdateService = Executors.newFixedThreadPool(4,
                new ThreadFactoryBuilder().setDaemon(true).setNameFormat("ProjectCounterUpdateThread-%d").build());

        NextIdGenerator(final ProjectManager projectManager, final OfBizDelegator delegator)
        {
            this.ofBizDelegator = delegator;
            this.projectCounterCache = new ProjectCounterCache(projectManager);
        }

        private void updateProject(final Long projectId, long current)
        {
            // do not cause a ProjectUpdated event , and store always happens in a separate
            // auto commit
            Future<Void> futureUpdater = projectCountUpdateService.submit(new ProjectCountUpdater(ofBizDelegator, projectId, current));
            try
            {
                futureUpdater.get();
            }
            catch (Exception e)
            {
                log.error("Cannot update project counter due to " + e.getMessage());
            }
        }


        private final LockManager<Project> lockManager = LockManagers.weakLockManager(new Function<Project, Long>()
        {
            @Override
            public Long get(final Project input)
            {
                return input.getId();
            }
        });

        long getNextId(final Project project)
        {
            if (project == null)
            {
                throw new IllegalArgumentException();
            }
            long incCount;
            final Long id = project.getId();
            final String projectKey = project.getKey();
            try
            {
                incCount = lockManager.withLock(project, new Callable<Long>()
                {
                    @Override
                    public Long call()
                    {
                        Long result;
                        do
                        {
                            result = projectCounterCache.getNextCounterForProject(id);
                        }
                        while (counterAlreadyExists(result, projectKey)); //if for some reason the key already exists, then keep searching
                        updateProject(id, result);
                        return result;
                    }
                });
            }
            catch (final RuntimeException e)
            {
                throw e;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
            return incCount;
        }

        long getCurrentCounterForProject(final long id)
        {
            return projectCounterCache.getCurrentCounterForProject(id);
        }

        public void refresh()
        {
            projectCounterCache.refresh();
        }

        public void newProject(Long projectId)
        {
            projectCounterCache.newProject(projectId);
        }

        /**
             * This is a sanity check to ensure that we are only giving out project keys that haven't already been given out.
             * <p> In an ideal world, this should never return true. </p> Note that this method isn't guaranteed to avoid
             * duplicates, as it will only work if the Issue has already been inserted in the DB.
             *
             * @param incCount the suggested Issue number
             * @param projectKey The project
             * @return true if this Issue Key already exists in the DB.
             */
        private boolean counterAlreadyExists(final long incCount, final String projectKey)
        {
            final String issueKey = projectKey + "-" + incCount;
            final List<GenericValue> result = ofBizDelegator.findByAnd("Issue", FieldMap.build("key", issueKey));
            final boolean alreadyExists = !result.isEmpty();
            if (alreadyExists)
            {
                log.error("Existing issue found for key " + issueKey + ". Incrementing key.");
            }
            return (alreadyExists);
        }

        public void resetCounter(final Project project, final long counter)
        {
            lockManager.withLock(project, new Runnable()
            {
                @Override
                public void run()
                {
                    Long id = project.getId();
                    new ProjectCountUpdater(ofBizDelegator, id, counter).call();
                    projectCounterCache.setCurrentCounterForProject(id, counter);
                }
            });

        }
    }


    private static class ProjectCountUpdater implements Callable<Void>
    {
        final OfBizDelegator delegator;
        private final Long projectId;
        private final long counter;

        public ProjectCountUpdater(OfBizDelegator delegator, Long projectId, long counter)
        {
            this.projectId = projectId;
            this.counter = counter;
            this.delegator = delegator;
        }

        @Override
        public Void call()
        {
            Update.into("Project").set("counter", counter).whereEqual("id", projectId).execute(delegator);
            return null;
        }
    }

    /**
     * An eagerly-initalized cache of project counters.
     *
     * @since v4.4
     */
    private static class ProjectCounterCache
    {
        private final ConcurrentMap<Long, AtomicLong> projectCounterCache =
                new MapMaker().makeComputingMap(new com.google.common.base.Function<Long, AtomicLong>()
                {
                    @Override
                    public AtomicLong apply(@Nullable Long projectId)
                    {
                        return new AtomicLong(readCounterFromDatabase(projectId));
                    }
                });
        private final ProjectManager projectManager;

        public ProjectCounterCache(ProjectManager projectManager)
        {
            this.projectManager = projectManager;
            //greedy evaluation
            refresh();
        }

        /**
         * eagerly refresh the cache.
         */
        public void refresh()
        {
            populateCache();
        }

        /**
         * add a new project to the cache
         * @param projectId id of the project to populate into the cache.
         */
        public void newProject(Long projectId)
        {
            populateCache(projectId, 0L);
        }

        public long getNextCounterForProject(long id)
        {
            return projectCounterCache.get(id).incrementAndGet();
        }

        public long getCurrentCounterForProject(long id)
        {
            AtomicLong counter = projectCounterCache.get(id);
            return counter != null ? counter.longValue() : 0;
        }

        private void populateCache()
        {
            projectCounterCache.clear();
            for (GenericValue project : projectManager.getProjects())
            {
                populateCache(project.getLong("id"), project.getLong("counter"));
            }
        }

        private void populateCache(Long projectId, Long counter)
        {
            counter = counter == null ? 0 : counter;
            projectCounterCache.putIfAbsent(projectId, new AtomicLong(counter));
        }
        private void setCurrentCounterForProject(Long projectId, long counter)
        {
            projectCounterCache.put(projectId, new AtomicLong(counter));
        }

        private long readCounterFromDatabase(Long projectId)
        {
            GenericValue project = projectManager.getProject(projectId);
            if (project != null)
            {
                return project.getLong("counter");
            }
            else
            {
                return 0;
            }
        }
    }
}
