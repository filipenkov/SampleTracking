/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.type.SecurityType;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class AbstractSchemeManager implements SchemeManager
{
    private static final Logger log = Logger.getLogger(AbstractSchemeManager.class);

    // TODO we need to rewrite this cache to store Scheme instances instead of GVs. Migration needs a performant way of navigating from Scheme to GV to support old calls
    private final ConcurrentMap<Long, ConcurrentMap<String, List<GenericValue>>> projectSchemeCache = new ConcurrentHashMap<Long, ConcurrentMap<String, List<GenericValue>>>();
    protected final ProjectManager projectManager;
    private final AbstractSchemeTypeManager typeManager;
    private final PermissionContextFactory permissionContextFactory;
    protected final SchemeFactory schemeFactory;
    private AssociationManager associationManager;
    private OfBizDelegator ofBizDelegator;
    private final GroupManager groupManager;

    protected AbstractSchemeManager(final ProjectManager projectManager, final AbstractSchemeTypeManager typeManager, final PermissionContextFactory permissionContextFactory, final SchemeFactory schemeFactory, final AssociationManager associationManager, final OfBizDelegator ofBizDelegator, GroupManager groupManager)
    {
        this.projectManager = projectManager;
        this.typeManager = typeManager;
        this.permissionContextFactory = permissionContextFactory;
        this.schemeFactory = schemeFactory;
        this.associationManager = associationManager;
        this.ofBizDelegator = ofBizDelegator;
        this.groupManager = groupManager;
    }

    public void onClearCache(final ClearCacheEvent event)
    {
        flushProjectSchemes();
    }

    public abstract String getSchemeEntityName();

    public abstract String getEntityName();

    public abstract String getSchemeDesc();

    public abstract String getDefaultNameKey();

    public abstract String getDefaultDescriptionKey();

    /**
     * Identifies whether this scheme manager makes its schemes associated with {@link
     * com.atlassian.jira.project.Project projects} or something else. This is here for historic reasons as schemes are
     * now always associated with projects. This means you should not override this.
     *
     * @return @{link SchemeManager#PROJECT_ASSOCIATION}
     * @deprecated Just assume all schemes are project association schemes.
     */
    @Deprecated
    public String getAssociationType()
    {
        return PROJECT_ASSOCIATION;
    }

    public abstract GenericValue copySchemeEntity(GenericValue scheme, GenericValue entity)
            throws GenericEntityException;

    @Override
    public GenericValue getScheme(final Long id) throws GenericEntityException
    {
        return ofBizDelegator.findById(getSchemeEntityName(), id);
    }

    @Override
    public Scheme getSchemeObject(final Long id) throws DataAccessException
    {
        return getSchemeObject(EasyMap.build("id", id));
    }

    @Override
    public Scheme getSchemeObject(final String name) throws DataAccessException
    {
        return getSchemeObject(EasyMap.build("name", name));
    }

    private Scheme getSchemeObject(Map gvParams)
    {
        final GenericValue schemeGv = EntityUtil.getOnly(ofBizDelegator.findByAnd(getSchemeEntityName(), gvParams));
        if (schemeGv != null)
        {
            return schemeFactory.getScheme(schemeGv);
        }
        else
        {
            return null;
        }
    }

    @Override
    public GenericValue getScheme(final String name) throws DataAccessException
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd(getSchemeEntityName(), EasyMap.build("name", name)));
    }

    @Override
    public List<GenericValue> getSchemes() throws DataAccessException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> schemes = ofBizDelegator.findAll(getSchemeEntityName());
        Collections.sort(schemes, OfBizComparators.NAME_COMPARATOR);
        return schemes;
    }

    @Override
    public List<Scheme> getSchemeObjects() throws DataAccessException
    {
        final List<GenericValue> schemeGvs = ofBizDelegator.findAll(getSchemeEntityName());
        Collections.sort(schemeGvs, OfBizComparators.NAME_COMPARATOR);
        return schemeFactory.getSchemes(schemeGvs);
    }

    @Override
    public List<Scheme> getAssociatedSchemes(final boolean withEntitiesComparable) throws DataAccessException
    {
        final List<Scheme> associatedSchemes = new ArrayList<Scheme>();
        try
        {
            final List<GenericValue> schemes = getSchemes();
            for (final GenericValue schemeGV : schemes)
            {
                if (getProjects(schemeGV).size() != 0)
                {
                    if (withEntitiesComparable)
                    {
                        associatedSchemes.add(schemeFactory.getSchemeWithEntitiesComparable(schemeGV));
                    }
                    else
                    {
                        associatedSchemes.add(schemeFactory.getScheme(schemeGV));
                    }
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        return associatedSchemes;
    }

    @Override
    public List<Scheme> getUnassociatedSchemes() throws DataAccessException
    {
        final List<Scheme> unassociatedSchemes = new ArrayList<Scheme>();
        try
        {
            final List<GenericValue> schemes = getSchemes();
            for (final GenericValue schemeGV : schemes)
            {
                if (getProjects(schemeGV).isEmpty())
                {
                    unassociatedSchemes.add(schemeFactory.getScheme(schemeGV));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        Collections.sort(unassociatedSchemes, new SchemeComparator());
        return unassociatedSchemes;
    }

    @SuppressWarnings ("unchecked")
    @Override
    public List<GenericValue> getSchemes(final GenericValue project) throws GenericEntityException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Cannot get schemes for null project");
        }
        //only cache project associations
        return cacheAndReturnProjectSchemes(project, getSchemeEntityName());
    }

    public Scheme getSchemeFor(Project project)
    {
        try
        {
            final List<GenericValue> schemes = getSchemes(project.getGenericValue());
            if (schemes.isEmpty())
            {
                return null;
            }
            if (schemes.size() > 1)
            {
                throw new IllegalStateException("Too many " + getSchemeEntityName() + " schemes found for Project " + project.getKey());
            }
            return schemeFactory.getScheme(schemes.iterator().next());
        }
        catch (GenericEntityException ex)
        {
            throw new DataAccessException(ex);
        }
    }


    @Override
    public GenericValue getEntity(final Long id) throws GenericEntityException
    {
        return ofBizDelegator.findById(getEntityName(), id);
    }

    @Override
    public List<GenericValue> getEntities(final String type, final String parameter) throws GenericEntityException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> result = ofBizDelegator.findByAnd(getEntityName(),
                EasyMap.build("type", type, "parameter", parameter));
        return result;
    }

    @Override
    public List<GenericValue> getEntities(final GenericValue scheme) throws GenericEntityException
    {
        @SuppressWarnings ("unchecked")
        final List<GenericValue> related = scheme.getRelated("Child" + getEntityName());
        return related;
    }

    @Override
    public boolean schemeExists(final String name) throws GenericEntityException
    {
        return (getScheme(name) != null);
    }

    @Override
    public GenericValue createScheme(final String name, final String description) throws GenericEntityException
    {
        if (!schemeExists(name))
        {
            flushProjectSchemes();
            return createSchemeGenericValue(EasyMap.build("name", name, "description", description));
        }
        else
        {
            throw new GenericEntityException("Could not create " + getSchemeDesc() + " Scheme with name:" + name + " as it already exists.");
        }
    }

    @Override
    public Scheme createSchemeObject(final String name, final String description)
    {
        try
        {
            return schemeFactory.getScheme(createScheme(name, description));
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public Scheme createSchemeAndEntities(final Scheme scheme) throws DataAccessException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException();
        }

        GenericValue schemeGV;

        try
        {
            schemeGV = createScheme(scheme.getName(), scheme.getDescription());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }

        final List<GenericValue> entityGVs = new ArrayList<GenericValue>();
        // Now create all the schemes entities
        @SuppressWarnings ("unchecked")
        final Collection<SchemeEntity> schemeEntities = scheme.getEntities();
        for (final SchemeEntity schemeEntity : schemeEntities)
        {
            try
            {
                entityGVs.add(createSchemeEntity(schemeGV, schemeEntity));
            }
            catch (final GenericEntityException e)
            {
                throw new DataAccessException(e);
            }
        }

        return schemeFactory.getScheme(schemeGV, entityGVs);
    }

    @Override
    public void updateScheme(final GenericValue entity) throws GenericEntityException
    {
        entity.store();
        flushProjectSchemes();
    }

    @Override
    public void updateScheme(final Scheme scheme) throws DataAccessException
    {
        try
        {
            final GenericValue schemeEntity = getScheme(scheme.getId());
            schemeEntity.setString("name", scheme.getName());
            schemeEntity.setString("description", scheme.getDescription());
            updateScheme(schemeEntity);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void deleteScheme(final Long id) throws GenericEntityException
    {
        // We want to make sure we never delete a scheme that has an id of 0, this means the scheme is a
        // default (i.e. DefaultPermissionScheme, we always want one in JIRA). JRA-11705
        if ((id != null) && (id != 0))
        {
            final GenericValue scheme = getScheme(id);
            associationManager.removeAssociationsFromSink(scheme);
            scheme.removeRelated("Child" + getEntityName());
            scheme.remove();
            flushProjectSchemes();
        }
    }

    @Deprecated
    @Override
    public void addSchemeToProject(final GenericValue project, final GenericValue scheme) throws GenericEntityException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("The project passed can not be null.");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("The first argument passed must be a project, " + project.getEntityName() + " is not.");
        }

        final List<GenericValue> schemes = getSchemes(project);
        if (!schemes.contains(scheme))
        {
            associationManager.createAssociation(project, scheme, getAssociationType());
        }
        flushProjectSchemes();
    }

    @Override
    public void addSchemeToProject(final Project project, final Scheme scheme) throws DataAccessException
    {
        if (project == null)
        {
            throw new IllegalArgumentException("The project passed can not be null.");
        }
        if (scheme == null)
        {
            throw new IllegalArgumentException("The sheme passed can not be null.");
        }

        try
        {
            final List<GenericValue> schemes = getSchemes(project.getGenericValue());
            final GenericValue schemeGV = getScheme(scheme.getId());
            if (!schemes.contains(schemeGV))
            {
                associationManager.createAssociation(project.getGenericValue(), schemeGV, getAssociationType());
            }
            flushProjectSchemes();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void removeSchemesFromProject(final Project project) throws DataAccessException
    {
        try
        {
            removeSchemesFromProject(project.getGenericValue());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Deprecated
    @Override
    public void removeSchemesFromProject(final GenericValue project) throws GenericEntityException
    {
        final List<GenericValue> schemes = getSchemes(project);
        for (final GenericValue scheme : schemes)
        {
            associationManager.removeAssociation(project, scheme, getAssociationType());
        }
        flushProjectSchemes();
    }

    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        try
        {
            getEntity(id).remove();
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Deprecated
    @Override
    public List<GenericValue> getProjects(final GenericValue scheme) throws GenericEntityException
    {
        @SuppressWarnings ("unchecked")
        final List<Long> projectIds = associationManager.getSourceIdsFromSink(scheme, "Project", getAssociationType());

        // it is faster to go the project manager (which is presumably cached) than to load it via association manager
        // if we ever do database joins, then we may be able to remove this
        final List<GenericValue> projects = new ArrayList<GenericValue>();
        for (final Long projectId : projectIds)
        {
            projects.add(projectManager.getProject(projectId));
        }
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    @Override
    public List<Project> getProjects(final Scheme scheme) throws DataAccessException
    {
        if ((scheme == null) || (scheme.getId() == null))
        {
            throw new IllegalArgumentException("The scheme and the schemes id can not be null");
        }

        try
        {
            final GenericValue schemeGV = getScheme(scheme.getId());
            return new ArrayList<Project>(ComponentManager.getInstance().getProjectFactory().getProjects(getProjects(schemeGV)));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue createDefaultScheme() throws GenericEntityException
    {
        if (getDefaultScheme() == null)
        {
            return createSchemeGenericValue(EasyMap.build("id", 0L, "name", getI18nTextWithDefaultNullCheck(getDefaultNameKey(),
                    "Default " + getSchemeDesc() + " Scheme"), "description", getI18nTextWithDefaultNullCheck(getDefaultDescriptionKey(),
                    "This is the default " + getSchemeDesc() + " Scheme. Any new projects that are created will be assigned this scheme")));
        }
        else
        {
            return getDefaultScheme();
        }
    }

    @Override
    public boolean removeEntities(final String type, final String parameter) throws RemoveException
    {
        if (type == null)
        {
            throw new IllegalArgumentException("Type passed must not be null");
        }

        if (parameter == null)
        {
            throw new IllegalArgumentException("Parameter passed must not be null");
        }

        try
        {
            final List<GenericValue> entities = getEntities(type, parameter);
            ofBizDelegator.removeAll(entities);
            return true;
        }
        catch (final GenericEntityException e)
        {
            throw new RemoveException(e);
        }
    }

    @Override
    public boolean removeEntities(final GenericValue scheme, final Long entityTypeId) throws RemoveException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed to this function must not be NULL");
        }

        try
        {
            final List<GenericValue> entities = getEntities(scheme, entityTypeId);
            ofBizDelegator.removeAll(entities);
            return true;
        }
        catch (final GenericEntityException e)
        {
            throw new RemoveException(e);
        }
    }

    @Override
    public GenericValue getDefaultScheme() throws GenericEntityException
    {
        return ofBizDelegator.findById(getSchemeEntityName(), 0L);
    }

    @Override
    public Scheme getDefaultSchemeObject()
    {
        try
        {
            final GenericValue defaultSchemeGV = getDefaultScheme();
            if (defaultSchemeGV == null)
            {
                return null;
            }
            return schemeFactory.getScheme(defaultSchemeGV);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public void addDefaultSchemeToProject(final GenericValue project) throws GenericEntityException
    {
        final GenericValue scheme = getDefaultScheme();

        if (scheme != null)
        {
            final List<GenericValue> schemes = getSchemes(project);
            if (!schemes.contains(scheme))
            {
                associationManager.createAssociation(project, scheme, getAssociationType());
            }
        }
        flushProjectSchemes();
    }

    @Override
    public void addDefaultSchemeToProject(final Project project) throws DataAccessException
    {
        try
        {
            addDefaultSchemeToProject(project.getGenericValue());
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public GenericValue copyScheme(final GenericValue scheme) throws GenericEntityException
    {
        if (scheme == null)
        {
            return null;
        }
        String name = ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof",
                scheme.getString("name"));

        int j = 2;
        while (true)
        {
            // check if the scheme already exists, and if it does, add a number to the name
            if (schemeExists(name))
            {
                name = ComponentManager.getInstance().getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyxof",
                        String.valueOf(j++), scheme.getString("name"));
            }
            else
            {
                break; // the scheme does not exist, so break the while loop
            }
        }

        final GenericValue newScheme = createScheme(name, scheme.getString("description"));
        final List<GenericValue> origEntities = getEntities(scheme);
        for (final GenericValue entity : origEntities)
        {
            copySchemeEntity(newScheme, entity);
        }
        return newScheme;
    }

    @Override
    public Scheme copyScheme(Scheme scheme)
    {
        try
        {
            GenericValue oldScheme = getScheme(scheme.getId());
            GenericValue newScheme = copyScheme(oldScheme);
            return schemeFactory.getScheme(newScheme);
        }
        catch (GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    /**
     * This function caches schemes for a project if they are not cached already and returns the schemes.
     */
    private List<GenericValue> cacheAndReturnProjectSchemes(final GenericValue project, final String entityName)
            throws GenericEntityException
    {
        final Long projectId = project.getLong("id");
        ConcurrentMap<String, List<GenericValue>> projectEntry = projectSchemeCache.get(projectId);
        while (projectEntry == null)
        {
            //Create the cache hashmap for this project
            projectSchemeCache.putIfAbsent(projectId, new ConcurrentHashMap<String, List<GenericValue>>());
            projectEntry = projectSchemeCache.get(projectId);
        }

        if (!projectEntry.containsKey(entityName))
        {
            //Add a list to the project for this entity
            @SuppressWarnings ("unchecked")
            final List<GenericValue> sinkFromSource = associationManager.getSinkFromSource(project, entityName,
                    getAssociationType(), false);
            projectEntry.putIfAbsent(entityName, sinkFromSource);
        }
        return Collections.unmodifiableList(projectEntry.get(getSchemeEntityName()));
    }

    protected void flushProjectSchemes()
    {
        projectSchemeCache.clear();
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final Project project)
    {
        return getGroups(entityTypeId, project.getGenericValue());
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final GenericValue project)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Project passed can NOT be null");
        }
        if (!"Project".equals(project.getEntityName()))
        {
            throw new IllegalArgumentException("Project passed must be a project not a " + project.getEntityName());
        }

        final Set<Group> groups = new HashSet<Group>();

        try
        {
            final List<GenericValue> schemes = getSchemes(project);
            for (final GenericValue scheme : schemes)
            {
                final List<GenericValue> entity = getEntities(scheme, GroupDropdown.DESC, entityTypeId);
                for (final GenericValue permission : entity)
                {
                    groups.add(groupManager.getGroup(permission.getString("parameter")));
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e.getMessage());
            e.printStackTrace();
        }

        return groups;
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final GenericValue projectOrIssue)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(projectOrIssue));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final Project project)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(project));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final Issue issue)
    {
        return getUsers(permissionId, permissionContextFactory.getPermissionContext(issue));
    }

    @Override
    public Collection<User> getUsers(final Long permissionId, final PermissionContext ctx)
    {
        final Set<User> users = new HashSet<User>();

        final Map<?, ?> permTypes = typeManager.getTypes();
        try
        {
            final List<GenericValue> schemes = getSchemes(ctx.getProject());
            for (final GenericValue scheme : schemes)
            {
                final List<GenericValue> entities = getEntities(scheme, permissionId);

                for (final GenericValue entity : entities)
                {
                    final SecurityType secType = (SecurityType) permTypes.get(entity.getString("type"));
                    try
                    {
                        @SuppressWarnings ("unchecked")
                        final Set<User> usersToAdd = secType.getUsers(ctx, entity.getString("parameter"));
                        for (User user : usersToAdd)
                        {
                            if (user.isActive())
                            {
                                users.add(user);
                            }
                        }
                    }
                    catch (final IllegalArgumentException e)
                    {
                        // If the entered custom field id is incorrect
                        log.warn(e.getMessage(), e);
                    }
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e.getMessage(), e);
        }
        return users;
    }

    protected GenericValue createSchemeGenericValue(final Map<String, Object> values) throws GenericEntityException
    {
        return EntityUtils.createValue(getSchemeEntityName(), values);
    }

    private String getI18nTextWithDefaultNullCheck(final String key, final String defaultResult)
    {
        if (key == null)
        {
            return defaultResult;
        }
        final String result = getApplicationI18n().getText(key);
        if (result.equals(key))
        {
            return defaultResult;
        }
        else
        {
            return result;
        }
    }

    I18nHelper getApplicationI18n()
    {
        return new I18nBean();
    }
}
