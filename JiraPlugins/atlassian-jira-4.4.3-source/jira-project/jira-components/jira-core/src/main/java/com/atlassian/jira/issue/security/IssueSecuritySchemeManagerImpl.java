package com.atlassian.jira.issue.security;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.ClearCacheEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.permission.PermissionContextFactory;
import com.atlassian.jira.permission.PermissionTypeManager;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.scheme.AbstractSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeFactory;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.security.type.SecurityType;
import org.apache.commons.collections.map.LRUMap;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IssueSecuritySchemeManagerImpl extends AbstractSchemeManager implements IssueSecuritySchemeManager, Startable
{
    private static final Logger log = Logger.getLogger(IssueSecuritySchemeManagerImpl.class);

    private static final String SCHEME_ENTITY_NAME = "IssueSecurityScheme";
    private static final String ISSUE_SECURITY_ENTITY_NAME = "SchemeIssueSecurities";
    private static final String SCHEME_DESC = "Issue Security";
    private static final String DEFAULT_NAME_KEY = "admin.schemes.security.default";
    private static final String DEFAULT_DESC_KEY = "admin.schemes.security.default.desc";

    private Map cache;
    private final EventPublisher eventPublisher;
    private AssociationManager associationManager;
    private OfBizDelegator ofBizDelegator;

    public IssueSecuritySchemeManagerImpl(ProjectManager projectManager, PermissionTypeManager permissionTypeManager,
            PermissionContextFactory permissionContextFactory, SchemeFactory schemeFactory, EventPublisher eventPublisher,
            final AssociationManager associationManager, final OfBizDelegator ofBizDelegator, final GroupManager groupManager)
    {
        super(projectManager, permissionTypeManager, permissionContextFactory, schemeFactory, associationManager, ofBizDelegator, groupManager);
        this.eventPublisher = eventPublisher;
        this.associationManager = associationManager;
        this.ofBizDelegator = ofBizDelegator;
        cache = Collections.synchronizedMap(new LRUMap(1000));
    }

    public void start() throws Exception
    {
        eventPublisher.register(this);
    }

    @EventListener
    public void onClearCache(final ClearCacheEvent event)
    {
        super.onClearCache(event);
        clearCache();
    }

    public String getSchemeEntityName()
    {
        return SCHEME_ENTITY_NAME;
    }

    public String getEntityName()
    {
        return ISSUE_SECURITY_ENTITY_NAME;
    }

    public String getSchemeDesc()
    {
        return SCHEME_DESC;
    }

    public String getDefaultNameKey()
    {
        return DEFAULT_NAME_KEY;
    }

    public String getDefaultDescriptionKey()
    {
        return DEFAULT_DESC_KEY;
    }

    private void clearCache()
    {
        if (log.isDebugEnabled())
        {
            log.debug("Clearing issue security scheme cache, had " + cache.size() + " entries");
        }

        cache.clear();
    }

    public List<GenericValue> getEntities(GenericValue scheme) throws GenericEntityException
    {
        return getEntitiesAndCache(EasyMap.build("scheme", scheme.getLong("id")));
    }

    public List<GenericValue> getEntities(GenericValue scheme, Long entityTypeId) throws GenericEntityException
    {
        return getEntitiesAndCache(EasyMap.build("scheme", scheme.getLong("id"), "security", entityTypeId));
    }

    public List<GenericValue> getEntitiesBySecurityLevel(Long securityLevelId) throws GenericEntityException
    {
        return getEntitiesAndCache(EasyMap.build("security", securityLevelId));
    }

    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter)
    {
        Collection<GenericValue> schemes;

        Collection<GenericValue> entities = ofBizDelegator.findByAnd(ISSUE_SECURITY_ENTITY_NAME, EasyMap.build("type", type, "parameter", parameter));
        Set<Long> schemeIds = new HashSet<Long>();
        List<EntityExpr> entityConditions = new ArrayList<EntityExpr>();
        for (GenericValue schemeEntity : entities)
        {
            // This is not needed if we can do a distinct select
            schemeIds.add(schemeEntity.getLong("scheme"));
        }
        for (Long schemeId : schemeIds)
        {
            entityConditions.add(new EntityExpr("id", EntityOperator.EQUALS, schemeId));
        }

        if (!entityConditions.isEmpty())
        {
            schemes = ofBizDelegator.findByOr(SCHEME_ENTITY_NAME, entityConditions, Collections.EMPTY_LIST);
        }
        else
        {
            schemes = Collections.emptyList();
        }
        return schemes;
    }

    private List getEntitiesAndCache(Map parameters) throws GenericEntityException
    {
        // TODO ! DANGER ! DANGER ! this code is all fucked up and needs to be FIXED !!
        List result = (List) cache.get(parameters);

        if (result == null)
        {
            result = ofBizDelegator.findByAnd(getEntityName(), parameters);

            if (result == null)
            {
                result = Collections.EMPTY_LIST;
            }

            if (log.isDebugEnabled())
            {
                log.debug("Caching " + result.size() + " entities for cache key: " + parameters);
            }

            cache.put(parameters, result);
        }

        return result;
    }

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param schemeTypeId The Id of the permission
     * @param parameter    The permission parameter (group name etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    public List<GenericValue> getEntities(GenericValue scheme, Long schemeTypeId, String parameter) throws GenericEntityException
    {
        return getEntitiesAndCache(EasyMap.build("scheme", scheme.getLong("id"), "security", schemeTypeId, "parameter", parameter));
    }

    //This one is for Workflows as the entity type is is a string
    public List<GenericValue> getEntities(GenericValue scheme, String entityTypeId) throws GenericEntityException
    {
        throw new IllegalArgumentException("Issue Security scheme IDs must be Long values.");
    }

    /**
     * Get all Generic Value issue security records for a particular scheme, type and Id
     *
     * @param scheme       The scheme that the permissions belong to
     * @param type         The type of the permission(Group, Current Reporter etc)
     * @param schemeTypeId The Id of the permission
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    public List<GenericValue> getEntities(GenericValue scheme, String type, Long schemeTypeId) throws GenericEntityException
    {
        return getEntitiesAndCache(EasyMap.build("scheme", scheme.getLong("id"), "security", schemeTypeId, "type", type));
    }

    public GenericValue createSchemeEntity(GenericValue scheme, SchemeEntity schemeEntity) throws GenericEntityException
    {
        if (!(schemeEntity.getEntityTypeId() instanceof Long))
        {
            throw new IllegalArgumentException("Issue Security Level IDs must be a long value.");
        }

        try
        {
            return EntityUtils.createValue(ISSUE_SECURITY_ENTITY_NAME, EasyMap.build("scheme", (scheme == null ? null : scheme.getLong("id")), "security", schemeEntity.getEntityTypeId(), "type", schemeEntity.getType(), "parameter", schemeEntity.getParameter()));
        }
        finally
        {
            clearCache();
        }
    }

    public GenericValue copySchemeEntity(GenericValue scheme, GenericValue entity) throws GenericEntityException
    {
        SchemeEntity schemeEntity = new SchemeEntity(entity.getString("type"), entity.getString("parameter"), entity.getLong("security"));
        return createSchemeEntity(scheme, schemeEntity);
    }

    /**
     * This method overrides the AbstractSchemeManager because within Issue Security schemes there is an extra level, which
     * is the table that holds the Security Levels for that Scheme. This is because with Issue Security schemes you can add and delete
     * the different levels of security. With other schemes this is not possible
     */
    public GenericValue copyScheme(GenericValue scheme) throws GenericEntityException
    {
        if (scheme != null)
        {
            try
            {
                String name = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyof", scheme.getString("name"));
                int j = 2;
                while (true)
                {
                    // check if the scheme already exists, and if it does, add a number to the name
                    if (schemeExists(name))
                    {
                        name = ComponentAccessor.getJiraAuthenticationContext().getI18nHelper().getText("common.words.copyxof", String.valueOf(j++), scheme.getString("name"));
                    }
                    else
                    {
                        break; // the scheme does not exist, so break the while loop
                    }
                }

                //Copy the original scheme
                GenericValue newScheme = createScheme(name, scheme.getString("description"));

                //for the scheme copy all security levels
                copySecurityLevels(newScheme, scheme);

                return newScheme;
            }
            finally
            {
                clearCache();
            }
        }
        else
        {
            return null;
        }
    }

    private void copySecurityLevels(GenericValue scheme, GenericValue oldScheme) throws GenericEntityException
    {
        //get all the security levels for this scheme
        List<GenericValue> levels = ofBizDelegator.findByAnd("SchemeIssueSecurityLevels", EasyMap.build("scheme", oldScheme.getLong("id")));

        //create the security levels for the new scheme
        for (GenericValue level : levels)
        {
            GenericValue newLevel = EntityUtils.createValue("SchemeIssueSecurityLevels", EasyMap.build("scheme", scheme.getLong("id"), "name", level.getString("name"), "description", level.getString("description")));

            //if this level is the default level for the old scheme then make it the default level for the new scheme also
            if (level.getLong("id").equals(oldScheme.getLong("defaultlevel")))
            {
                scheme.set("defaultlevel", newLevel.getLong("id"));
                scheme.store();
            }

            //copy all the securities for this security level and scheme and copy them also
            List<GenericValue> securities = ofBizDelegator.findByAnd(getEntityName(), EasyMap.build("scheme", oldScheme.getLong("id"), "security", level.getLong("id")));

            for (GenericValue security : securities)
            {
                createSchemeEntity(scheme, new SchemeEntity(security.getString("type"), security.getString("parameter"), newLevel.getLong("id")));
            }
        }
    }

    public boolean hasSchemeAuthority(Long entityType, GenericValue entity)
    {
        return hasPermission(entityType, entity, null);

    }

    /**
     * Checks to see if the user has access to issues of this security level.
     * If the user is not passed in then the check is made on the current user
     *
     * @param entityType    The security level to check against
     * @param issue         The issue
     * @param user          The user to check for the permission. User must NOT be null
     * @param issueCreation
     * @return true if the user is a member of the security level otherwise false
     */
    public boolean hasSchemeAuthority(Long entityType, GenericValue issue, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
    {
        if (user == null)
        {
            throw new IllegalArgumentException("User passed must NOT be null");
        }

        return hasPermission(entityType, issue, user);
    }

    private boolean hasPermission(Long entityType, GenericValue issue, com.atlassian.crowd.embedded.api.User user)
    {
        //If the entity type is null then there is no security set on the issue
        if (entityType == null)
        {
            return true;
        }
        if (issue == null)
        {
            throw new IllegalArgumentException("GenericValue passed must NOT be null");
        }
        if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("GenericValue passed must be an Issue and not " + issue.getEntityName());
        }

        //Get the project for the entity
        GenericValue project = null;
        project = ManagerFactory.getProjectManager().getProject(issue);

        try
        {
            //Get the issue security scheme associated to the project for this project
            List<GenericValue> schemes = ManagerFactory.getIssueSecuritySchemeManager().getSchemes(project);
            for (GenericValue scheme : schemes)
            {
                if (scheme != null)
                {
                    //try each type of Issue Security Type
                    Collection<SecurityType> types = ManagerFactory.getIssueSecurityTypeManager().getTypes().values();

                    //loop through each issue security type to see if the user has access to the issue.
                    //Once the permission is reached then we return right away
                    for (SecurityType type : types)
                    {
                        //Check to see if the permission exists
                        List<GenericValue> entities = getEntities(scheme, type.getType(), entityType);
                        for (GenericValue perm : entities)
                        {
                            if (perm != null)
                            {
                                if (user == null)
                                {
                                    if (type.hasPermission(issue, perm.getString("parameter")))
                                    {
                                        return true;
                                    }
                                }
                                else
                                {
                                    if (type.hasPermission(issue, perm.getString("parameter"), user, false))
                                    {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("Could not retrieve entites from the database", e);
            return false;
        }
        return false;
    }

    /**
     * Deletes a scheme from the database
     *
     * @param id Id of the scheme to be deleted
     * @throws GenericEntityException
     */
    public void deleteScheme(Long id) throws GenericEntityException
    {
        try
        {
            GenericValue scheme = getScheme(id);
            associationManager.removeAssociationsFromSink(scheme);
            scheme.removeRelated("Child" + getEntityName());
            scheme.removeRelated("Child" + "SchemeIssueSecurityLevels");
            scheme.remove();
        }
        finally
        {
            clearCache();
        }
    }

    public void deleteEntity(Long id) throws DataAccessException
    {
        try
        {
            super.deleteEntity(id);
        }
        finally
        {
            clearCache();
        }
    }

    public boolean removeEntities(GenericValue scheme, Long entityTypeId) throws RemoveException
    {
        try
        {
            return super.removeEntities(scheme, entityTypeId);
        }
        finally
        {
            clearCache();
        }
    }

    public GenericValue createScheme(String name, String description) throws GenericEntityException
    {
        try
        {
            return super.createScheme(name, description);
        }
        finally
        {
            clearCache();
        }
    }

    protected void flushProjectSchemes()
    {
        try
        {
            super.flushProjectSchemes();
        }
        finally
        {
            clearCache();
        }
    }

    /**
     * This method overrides the super implemntation in order to clear cache.
     *
     * @param type      type
     * @param parameter parameter
     * @return the original result of the call to super method
     * @throws RemoveException if super method throws it
     */
    public boolean removeEntities(String type, String parameter) throws RemoveException
    {
        try
        {
            return super.removeEntities(type, parameter);
        }
        finally
        {
            clearCache();
        }
    }

}
