/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import com.atlassian.core.action.ActionDispatcher;
import com.atlassian.core.action.ActionUtils;
import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.action.ActionNames;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.util.concurrent.Function;
import com.atlassian.util.concurrent.LockManager;
import com.atlassian.util.concurrent.LockManagers;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

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
    private final ActionDispatcher actionDispatcher;
    private final AvatarManager avatarManager;
    private final ProjectCategoryStore projectCategoryStore;

    private final NextIdGenerator nextIdGenerator = new NextIdGenerator(new ProjectRetriever()
    {
        @Override
        public boolean counterAlreadyExists(final long incCount, final GenericValue project)
        {
            return DefaultProjectManager.this.counterAlreadyExists(incCount, project);
        }

        @Override
        public GenericValue getProject(final Long id)
        {
            return DefaultProjectManager.this.getProject(id);
        }

        @Override
        public void updateProject(final GenericValue project)
        {
            DefaultProjectManager.this.updateProject(project);
        }
    });

    /**
     * Don't use this constructor.
     * <p/>
     * Use ManagerFactory.getProjectManager() instead.
     */
    public DefaultProjectManager()
    {
        this(new DefaultOfBizDelegator(CoreFactory.getGenericDelegator()), ComponentAccessor.getComponentOfType(NodeAssociationStore.class),
            ComponentManager.getComponentInstanceOfType(ProjectFactory.class), ComponentManager.getComponentInstanceOfType(ProjectRoleManager.class),
            ComponentManager.getComponentInstanceOfType(IssueManager.class), ComponentManager.getComponentInstanceOfType(ActionDispatcher.class),
            ComponentManager.getComponentInstanceOfType(AvatarManager.class), ComponentManager.getComponentInstanceOfType(UserManager.class),
            ComponentAccessor.getComponentOfType(ProjectCategoryStore.class), ComponentAccessor.getApplicationProperties());
    }

    public DefaultProjectManager(final OfBizDelegator delegator, final NodeAssociationStore nodeAssociationStore, final ProjectFactory projectFactory, final ProjectRoleManager projectRoleManager, final IssueManager issueManager, final ActionDispatcher actionDispatcher, AvatarManager avatarManager, UserManager userManager,
            ProjectCategoryStore projectCategoryStore, ApplicationProperties applicationProperties)
    {
        super(userManager, applicationProperties);
        this.delegator = delegator;
        this.nodeAssociationStore = nodeAssociationStore;
        this.projectFactory = projectFactory;
        this.projectRoleManager = projectRoleManager;
        this.issueManager = issueManager;
        this.actionDispatcher = actionDispatcher;
        this.avatarManager = avatarManager;
        this.projectCategoryStore = projectCategoryStore;
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

        return newProject;
    }

    // Create Methods --------------------------------------------------------------------------------------------------

    @Override
    public long getNextId(final GenericValue project)
    {
        return nextIdGenerator.getNextId(project);
    }

    /**
     * This is a sanity check to ensure that we are only giving out project keys that haven't already been given out.
     * <p> In an ideal world, this should never return true. </p> Note that this method isn't guaranteed to avoid
     * duplicates, as it will only work if the Issue has already been inserted in the DB.
     *
     * @param incCount the suggested Issue number
     * @param project The project
     * @return true if this Issue Key already exists in the DB.
     */
    private boolean counterAlreadyExists(final long incCount, final GenericValue project)
    {
        final String issueKey = project.getString("key") + "-" + incCount;
        final List<GenericValue> result = getDelegator().findByAnd("Issue", EasyMap.build("key", issueKey));
        final boolean alreadyExists = !result.isEmpty();
        if (alreadyExists)
        {
            log.error("Existing issue found for key " + issueKey + ". Incrementing key.");
        }
        return (alreadyExists);
    }

    @Override
    public void updateProject(final GenericValue updatedProject)
    {
        getDelegator().storeAll(CollectionBuilder.newBuilder(updatedProject).asList());
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String lead, final String url, final Long assigneeType, Long avatarId)
    {
        notNull("project", updatedProject);
        notNull("name", name);
        notNull("lead", lead);

        if (avatarId == null)
        {
            avatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
        }

        final GenericValue projectGV = updatedProject.getGenericValue();
        projectGV.setString("name", name);
        projectGV.setString("url", url);
        projectGV.setString("lead", lead);
        projectGV.setString("description", description);
        projectGV.set("assigneetype", assigneeType);
        projectGV.set("avatar", avatarId);
        delegator.store(projectGV);

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
            final GenericValue issueGV = issueManager.getIssue(issueId);
            // We have retrieved all issue ids for the project.
            if (issueGV != null)
            {
                try
                {
                    final ActionResult aResult = actionDispatcher.execute(ActionNames.ISSUE_DELETE, EasyMap.build("issue", issueGV, "dispatchEvent",
                        Boolean.FALSE, "permissionOverride", Boolean.TRUE));
                    ActionUtils.checkForErrors(aResult);
                }
                catch (final Exception e)
                {
                    log.error("Exception removing issues", e);
                    throw new RemoveException("Error removing issues: " + e, e);
                }
            }
            else
            {
                log.debug("Issue with id '" + issueId + "' was not find. Most likely it is a sub-task and has been deleted previously with its parent.");
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
        try
        {
            project.getGenericValue().remove();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue getProject(final Long id)
    {
        return getDelegator().findByPrimaryKey("Project", EasyMap.build("id", id));
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
        return EntityUtil.getOnly(getDelegator().findByAnd("Project", EasyMap.build("name", name)));
    }

    @Override
    public GenericValue getProjectByKey(final String key)
    {
        return EntityUtil.getOnly(getDelegator().findByAnd("Project", EasyMap.build("key", key)));
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
        return getDelegator().findByPrimaryKey("Component", EasyMap.build("id", id));
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public GenericValue getComponent(final GenericValue project, final String name)
    {
        // don't bother caching - only used when creating components anyway
        return EntityUtil.getOnly(getDelegator().findByAnd("Component", EasyMap.build("project", project.getLong("id"), "name", name)));
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
    public final Collection<GenericValue> getProjectsByLead(final com.opensymphony.user.User leadUser)
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
    public void refreshProjectDependencies(final GenericValue project)
    {}

    @Override
    public void refresh()
    {}

    ///CLOVER:OFF
    IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return ComponentManager.getComponentInstanceOfType(IssueSecurityLevelManager.class);
    }
    ///CLOVER:ON

    /**
     * Responsible for generating the next Issue Key.
     */
    static class NextIdGenerator
    {
        private final ProjectRetriever retriever;
        private final LockManager<GenericValue> lockManager = LockManagers.weakLockManager(new Function<GenericValue, Long>()
        {
            @Override
            public Long get(final GenericValue input)
            {
                return input.getLong("id");
            };
        });

        NextIdGenerator(final ProjectRetriever retriever)
        {
            this.retriever = retriever;
        }

        long getNextId(final GenericValue project)
        {
            if (project == null)
            {
                throw new IllegalArgumentException();
            }
            long incCount;
            final long id = project.getLong("id");
                try
                {
                    incCount = lockManager.withLock(project, new Callable<Long>()
                    {
                        @Override
                        public Long call()
                        {
                            final GenericValue project = retriever.getProject(id);
                            Long result = project.getLong("counter");
                            do
                            {
                                result++;
                            }
                            while (retriever.counterAlreadyExists(result, project)); //if for some reason the key already exists, then keep searching
                            project.set("counter", result);
                            retriever.updateProject(project);
                            return result;
                        };
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
    }

    interface ProjectRetriever
    {
        GenericValue getProject(Long long1);

        boolean counterAlreadyExists(long incCount, GenericValue project);

        void updateProject(GenericValue project);
    }
}
