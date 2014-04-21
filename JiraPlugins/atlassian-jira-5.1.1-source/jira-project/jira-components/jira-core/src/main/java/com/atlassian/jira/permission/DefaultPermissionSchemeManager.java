/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.cache.GoogleCacheInstruments;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.event.permission.PermissionAddedEvent;
import com.atlassian.jira.event.permission.PermissionDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCopiedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeCreatedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeDeletedEvent;
import com.atlassian.jira.event.permission.PermissionSchemeUpdatedEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.scheme.SchemeType;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

/**
 * This class is used to handle Permission Schemes.
 * <p>
 * Permission Schemes are created, removed and edited through this class
 */
public class DefaultPermissionSchemeManager extends AbstractSchemeManager implements PermissionSchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(DefaultPermissionSchemeManager.class);

    private static final String SCHEME_ENTITY_NAME = "PermissionScheme";
    private static final String PERMISSION_ENTITY_NAME = "SchemePermissions";
    private static final String SCHEME_DESC = "Permission";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.permissions.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.permissions.default.desc";

    private final Cache<Long, SchemeEntityCacheEntry> schemeEntityCache = CacheBuilder.newBuilder().build(
            new CacheLoader<Long, SchemeEntityCacheEntry>()
            {
                @Override
                public SchemeEntityCacheEntry load(Long key) throws Exception
                {
                    SchemeEntityCacheEntry cacheEntry = new SchemeEntityCacheEntry();
                    cacheEntry.load(key);
                    return cacheEntry;
                }
            }
    );

    private final PermissionTypeManager permissionTypeManager;
    private final OfBizDelegator delegator;
    private final EventPublisher eventPublisher;

    public DefaultPermissionSchemeManager(final ProjectManager projectManager, final PermissionTypeManager permissionTypeManager,
            final PermissionContextFactory permissionContextFactory, final OfBizDelegator delegator,
            final SchemeFactory schemeFactory, final AssociationManager associationManager, final GroupManager groupManager,
            final EventPublisher eventPublisher)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory, associationManager, delegator, groupManager);
        this.permissionTypeManager = permissionTypeManager;
        this.delegator = delegator;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Registers this CachingFieldConfigContextPersister's cache in the JIRA instrumentation.
     *
     * @throws Exception
     */
    @Override
    public void start() throws Exception
    {
        new GoogleCacheInstruments(DefaultPermissionSchemeManager.class.getSimpleName()).addCache(schemeEntityCache).install();
    }

    @Override
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        flushSchemeEntities();
    }

    @Override
    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    @Override
    public String getEntityName()
    {
        return PERMISSION_ENTITY_NAME;
    }

    @Override
    public String getSchemeDesc()
    {
        return SCHEME_DESC;
    }

    @Override
    public String getDefaultNameKey()
    {
        return DEFAULT_NAME_KEY;
    }

    @Override
    public String getDefaultDescriptionKey()
    {
        return DEFAULT_DESC_KEY;
    }

    @Override
    public Scheme createSchemeObject(final String name, final String description)
    {
        final Scheme scheme = super.createSchemeObject(name, description);

        if (scheme != null)
        {
            // Only publish the event in this method, and not createScheme() as it is deprecated
            eventPublisher.publish(new PermissionSchemeCreatedEvent(scheme));
        }

        return scheme;
    }

    @Override
    public Scheme copyScheme(Scheme scheme)
    {
        final Scheme result = super.copyScheme(scheme);

        if (result != null)
        {
            // Only publish the event in this method, and not copyScheme(GenericValue) as it is deprecated
            eventPublisher.publish(new PermissionSchemeCopiedEvent(scheme, result));
        }

        return result;
    }

    @Override
    public void updateScheme(Scheme scheme) throws DataAccessException
    {
        super.updateScheme(scheme);

        // Only publish the event in this method, and not updateScheme(GenericValue) as it is deprecated
        eventPublisher.publish(new PermissionSchemeUpdatedEvent(scheme));
    }

    @Override
    public void deleteScheme(Long id) throws GenericEntityException
    {
        super.deleteScheme(id);

        eventPublisher.publish(new PermissionSchemeDeletedEvent(id));
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId) throws GenericEntityException
    {
        final Long key = getSchemeEntityCacheKey(scheme);
        try
        {
            final List<GenericValue> genericValues = schemeEntityCache.get(key).getCacheByPermission().get(permissionId);
            if (genericValues == null)
            {
                return Collections.EMPTY_LIST;
            }
            return Collections.unmodifiableList(genericValues);
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter    The permission parameter (group name etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId, final String parameter) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionId), Collections.singletonMap("parameter", parameter));
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter    The permission parameter (group name etc)
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final Long permissionId, final String type, final String parameter) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionId), MapBuilder.build("type", type, "parameter", parameter));
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @param permissionId The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final String type, final Long permissionId) throws GenericEntityException
    {
        return EntityUtil.filterByAnd(getEntities(scheme, permissionId),  Collections.singletonMap("type", type));
    }

    //This one if for Workflow Scheme Manager as the entity type is is a string
    @Override
    public List<GenericValue> getEntities(final GenericValue scheme, final String entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Permission scheme IDs must be Long values.");
    }

    /**
     * Create a new permission record in the database
     *
     * @param scheme       The scheme that the permission record is associated with
     * @param schemeEntity The scheme entity object that is to be added to the scheme
     * @return The permission object
     * @throws GenericEntityException
     */
    @Override
    public GenericValue createSchemeEntity(final GenericValue scheme, final SchemeEntity schemeEntity) throws GenericEntityException
    {
        if (scheme == null)
        {
            throw new IllegalArgumentException("Scheme passed must NOT be null");
        }
        if (!(schemeEntity.getEntityTypeId() instanceof Long) && !(schemeEntity.getEntityTypeId() instanceof Integer))
        {
            throw new IllegalArgumentException("Permission scheme IDs must be long or int values, not:" + schemeEntity.getEntityTypeId().getClass());
        }
        if (schemeEntity.getType() == null)
        {
            throw new IllegalArgumentException("Type in SchemeEntity can NOT be null");
        }

        final GenericValue perm = EntityUtils.createValue(PERMISSION_ENTITY_NAME,
                MapBuilder.<String, Object>newBuilder("scheme", scheme.getLong("id"))
                .add("permission", schemeEntity.getEntityTypeId())
                .add("type", schemeEntity.getType())
                .add("parameter", schemeEntity.getParameter()).toMap());

        eventPublisher.publish(new PermissionAddedEvent(scheme.getLong("id"), schemeEntity));

        schemeEntityCache.invalidate(getSchemeEntityCacheKey(scheme));

        return perm;
    }

    /**
     * Deletes a permission from the database
     *
     * @param id The id of the permission to be deleted
     */
    @Override
    public void deleteEntity(final Long id) throws DataAccessException
    {
        super.deleteEntity(id);

        eventPublisher.publish(new PermissionDeletedEvent(id));

        flushSchemeEntities();
    }

    @Override
    public GenericValue copySchemeEntity(final GenericValue scheme, final GenericValue entity) throws GenericEntityException
    {
        final SchemeEntity schemeEntity = new SchemeEntity(entity.getString("type"), entity.getString("parameter"), entity.getLong("permission"));
        return createSchemeEntity(scheme, schemeEntity);
    }

    @Override
    public List<GenericValue> getEntities(final GenericValue scheme) throws GenericEntityException
    {
        final Long key = getSchemeEntityCacheKey(scheme);
        try
        {
            return Collections.unmodifiableList(schemeEntityCache.get(key).getCache());
        }
        catch (ExecutionException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes all scheme entities with this parameter
     *
     * @param type      the type of scheme entity you wish to remove 'user', 'group', 'projectrole'
     * @param parameter must NOT be null
     */
    @Override
    public boolean removeEntities(final String type, final String parameter) throws RemoveException
    {
        final boolean result = super.removeEntities(type, parameter);
        flushSchemeEntities();
        return result;
    }

    /**
     * Retrieves all the entites for this permission and then removes them.
     *
     * @param scheme       to remove entites from must NOT be null
     * @param permissionId to remove must NOT be a global permission
     * @return True is all the entities are removed
     * @throws RemoveException
     */
    @Override
    public boolean removeEntities(final GenericValue scheme, final Long permissionId) throws RemoveException
    {
        if (Permissions.isGlobalPermission(permissionId.intValue()))
        {
            throw new IllegalArgumentException("PermissionId passed must not be a global permissions " + permissionId.toString() + " is global");
        }

        final boolean result = super.removeEntities(scheme, permissionId);
        schemeEntityCache.invalidate(getSchemeEntityCacheKey(scheme));
        return result;
    }

    /**
     * Checks to see if there is an anyone permission for that permission type.
     * Specific permissions can things such as Current Reporter, Project Lead, Single User etc.
     *
     * @param permissionId The permission to check against, must not be global permission
     * @param project      The entity to check for the permission. This entity must be a project
     * @return true if the user has the permission otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(final Long permissionId, final GenericValue project)
    {
        return hasPermission(permissionId, project, null, false);
    }

    /**
     * Checks to see if the user has any specific permissions for that permission type.
     * Specific permissions can things such as Current Reporter, Project Lead, Single User etc.
     *
     * @param permissionId  The permission to check against, must not be global permission
     * @param project       The entity to check for the permission. This entity must be a project
     * @param user          The user to check for the permission. The user must NOT be null
     * @param issueCreation true if this call is for a "Create Issue" permission.
     * @return true if the user has the permission otherwise false
     */
    @Override
    public boolean hasSchemeAuthority(final Long permissionId, final GenericValue project, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("The user passed must not be null");
        }

        return hasPermission(permissionId, project, user, issueCreation);
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final Project project)
    {
        return getGroups(entityTypeId, project.getGenericValue());
    }

    @Override
    public Collection<Group> getGroups(final Long entityTypeId, final GenericValue project)
    {
        if (Permissions.isGlobalPermission(entityTypeId.intValue()))
        {
            throw new IllegalArgumentException("PermissionId passed can NOT be a global permission " + entityTypeId.toString() + " is a global");
        }

        return super.getGroups(entityTypeId, project);
    }

    /////////////// Private methods /////////////////////////////////////////////////////
    private boolean hasPermission(final Long permissionId, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation)
    {
        if (Permissions.isGlobalPermission(permissionId.intValue()))
        {
            throw new IllegalArgumentException("permissionId passed to this function must NOT be a global permission, " + permissionId + " is not");
        }
        if (entity == null)
        {
            throw new IllegalArgumentException("The entity passed must not be null");
        }
        if (!("Project".equals(entity.getEntityName()) || "Issue".equals(entity.getEntityName())))
        {
            throw new IllegalArgumentException("The entity passed must be a Project or an Issue not a " + entity.getEntityName());
        }

        try
        {
            final Map<?, ?> types = permissionTypeManager.getTypes();
            List<GenericValue> schemes = Collections.emptyList();
            if ("Project".equals(entity.getEntityName()))
            {
                //Get the permission scheme associated to the project for this project
                schemes = ManagerFactory.getPermissionSchemeManager().getSchemes(entity);
            }
            else if ("Issue".equals(entity.getEntityName()))
            {
                final GenericValue project = ManagerFactory.getProjectManager().getProject(entity);
                schemes = ManagerFactory.getPermissionSchemeManager().getSchemes(project);
            }

            for (final GenericValue scheme : schemes)
            {
                if (hasSchemePermission(permissionId, scheme, entity, user, issueCreation, types))
                {
                    return true;
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error(e, e);
            return false;
        }
        return false;
    }

    boolean hasSchemePermission(final Long permissionId, final GenericValue scheme, final GenericValue entity, final com.atlassian.crowd.embedded.api.User user, final boolean issueCreation) throws GenericEntityException
    {
        //Retrieve all scheme permissions
        final Map<?, ?> types = permissionTypeManager.getTypes();
        return hasSchemePermission(permissionId, scheme, entity, user, issueCreation, types);
    }

    private boolean hasSchemePermission(Long permissionId, GenericValue scheme, GenericValue entity, User user, boolean issueCreation, Map<?, ?> types)
            throws GenericEntityException
    {
        final List<GenericValue> entities = getEntities(scheme, permissionId);
        for (final GenericValue perm : entities)
        {
            if (perm != null)
            {
                final SchemeType type = (SchemeType) types.get(perm.getString("type"));
                if (user == null)
                {
                    if (type.hasPermission(entity, perm.getString("parameter")))
                    {
                        return true;
                    }
                }
                else
                {
                    if ((permissionId == null) || type.isValidForPermission(permissionId.intValue()))
                    {
                        if (type.hasPermission(entity, perm.getString("parameter"), user, issueCreation))
                        {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private Long getSchemeEntityCacheKey(final GenericValue scheme)
    {
        return scheme.getLong("id");
    }

    public void flushSchemeEntities()
    {
        schemeEntityCache.invalidateAll();
    }

    public Collection<GenericValue> getSchemesContainingEntity(final String type, final String parameter)
    {
        Collection<GenericValue> schemes;

        final Collection<GenericValue> entities = delegator.findByAnd(PERMISSION_ENTITY_NAME, EasyMap.build("type", type, "parameter", parameter));
        final Set<Long> schemeIds = new HashSet<Long>();
        final List<EntityCondition> entityConditions = new ArrayList<EntityCondition>();
        for (final GenericValue schemeEntity : entities)
        {
            // This is not needed if we can do a distinct select
            schemeIds.add(schemeEntity.getLong("scheme"));
        }
        for (final Long id : schemeIds)
        {
            entityConditions.add(new EntityExpr("id", EntityOperator.EQUALS, id));
        }

        if (!entityConditions.isEmpty())
        {
            schemes = delegator.findByOr(SCHEME_ENTITY_NAME, entityConditions, Collections.EMPTY_LIST);
        }
        else
        {
            schemes = Collections.emptyList();
        }
        return schemes;

    }

    private class SchemeEntityCacheEntry
    {
        private List<GenericValue> cache = null;
        private final Map<Long, List<GenericValue>> cacheByPermission = new HashMap<Long, List<GenericValue>>();

        public List<GenericValue> getCache()
        {
            return cache;
        }

        public Map<Long,  List<GenericValue>> getCacheByPermission()
        {
            return cacheByPermission;
        }

        public void load(Long key)
        {
            List<GenericValue> schemeEntities;
            try
            {
                GenericValue scheme = DefaultPermissionSchemeManager.super.getScheme(key);
                schemeEntities = DefaultPermissionSchemeManager.super.getEntities(scheme);
            }
            catch (GenericEntityException e)
            {
                throw new RuntimeException(e);
            }
            cache = schemeEntities;

            for (GenericValue entity : schemeEntities)
            {
                Long permissionId = entity.getLong("permission");
                List<GenericValue> entitiesForPermission = cacheByPermission.get(permissionId);
                if (entitiesForPermission == null)
                {
                    entitiesForPermission = new ArrayList<GenericValue>();
                    cacheByPermission.put(permissionId,entitiesForPermission);
                }
                entitiesForPermission.add(entity);
            }
        }
    }

}
