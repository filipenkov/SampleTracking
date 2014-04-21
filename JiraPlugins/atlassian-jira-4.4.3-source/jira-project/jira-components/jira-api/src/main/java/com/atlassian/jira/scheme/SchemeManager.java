/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.scheme;

import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.permission.PermissionContext;
import com.atlassian.jira.project.Project;
import com.opensymphony.user.Group;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

public interface SchemeManager
{
    /** The one and only "association type". */
    public static final String PROJECT_ASSOCIATION = "ProjectScheme";

    GenericValue getScheme(Long id) throws GenericEntityException;

    /**
     *
     * @return
     * @throws GenericEntityException
     * @deprecated use {@link #getSchemeObjects()}
     */
    List<GenericValue> getSchemes() throws GenericEntityException;

    List<Scheme> getAssociatedSchemes(boolean withEntitiesComparable) throws DataAccessException;

    /**
     *
     * @param name
     * @return
     * @throws GenericEntityException
     * @deprecated use @{link #getSchemeObject(String)}
     */
    GenericValue getScheme(String name) throws GenericEntityException;

    List<GenericValue> getSchemes(GenericValue project) throws GenericEntityException;

    boolean schemeExists(String name) throws GenericEntityException;

    GenericValue createScheme(String name, String description) throws GenericEntityException;

    Scheme createSchemeAndEntities(Scheme scheme) throws DataAccessException;

    GenericValue getEntity(Long id) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, Long entityTypeId) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, Long entityTypeId, String parameter) throws GenericEntityException;

    List<GenericValue> getEntities(GenericValue scheme, String type, Long entityTypeId) throws GenericEntityException;

    //This one is for Workflow Scheme Manager as the entity type is is a string
    List<GenericValue> getEntities(GenericValue scheme, String entityTypeId) throws GenericEntityException;

    void updateScheme(GenericValue entity) throws GenericEntityException;

    void updateScheme(Scheme scheme) throws DataAccessException;

    void deleteScheme(Long id) throws GenericEntityException;

    void addSchemeToProject(GenericValue project, GenericValue scheme) throws GenericEntityException;

    void addSchemeToProject(Project project, Scheme scheme) throws DataAccessException;

    void removeSchemesFromProject(GenericValue project) throws GenericEntityException;

    void removeSchemesFromProject(Project project) throws DataAccessException;

    GenericValue createSchemeEntity(GenericValue scheme, SchemeEntity entity) throws GenericEntityException;

    void deleteEntity(Long id) throws GenericEntityException;

    /**
     * Get the generic value projects.
     * @param scheme
     * @return
     * @throws GenericEntityException
     * @deprecated use {@link #getProjects(Scheme)} instead
     */
    @Deprecated
    List<GenericValue> getProjects(GenericValue scheme) throws GenericEntityException;

    List<Project> getProjects(Scheme scheme) throws DataAccessException;

    GenericValue createDefaultScheme() throws GenericEntityException;

    /**
     * Gets the default scheme. This should have an id of 0
     *
     * This does not work for the Default Notification scheme as it does not have an id of 0.
     *
     * @return The default scheme
     * @throws GenericEntityException
     */
    GenericValue getDefaultScheme() throws GenericEntityException;

    void addDefaultSchemeToProject(GenericValue project) throws GenericEntityException;

    GenericValue copyScheme(GenericValue scheme) throws GenericEntityException;

    /**
     * Checks anonymous permission of the given permission type for the given entity.
     *
     * @param entityType permission type.
     * @param entity     the entity to which permission is being checked.
     * @return true only if the anonymous user is permitted.
     */
    boolean hasSchemeAuthority(Long entityType, GenericValue entity);

    /**
     * Checks the given user's permission of the given permission type for the given entity.
     *
     * @param entityType    permission type.
     * @param entity        the entity to which permission is being checked.
     * @param user          the user.
     * @param issueCreation whether the permission is for creating an issue.
     * @return true only if the user is permitted.
     */
    boolean hasSchemeAuthority(Long entityType, GenericValue entity, com.atlassian.crowd.embedded.api.User user, boolean issueCreation);

    boolean removeEntities(GenericValue scheme, Long permissionId) throws RemoveException;

    /**
     * Get all entity records with a particular parameter
     *
     * @param type      The type of entity you wish to retrieve, eg 'user', 'group', 'projectrole'
     * @param parameter The parameter in the entity
     * @return List of (GenericValue) entities
     * @throws GenericEntityException
     */
    List<GenericValue> getEntities(String type, String parameter) throws GenericEntityException;

    /**
     * Removes all scheme entities with this parameter and type
     *
     * @param type      the 'type' of entity you are deleting, eg 'group', 'user', 'projectrole'
     * @param parameter must NOT be null
     */
    boolean removeEntities(String type, String parameter) throws RemoveException;

    Collection<Group> getGroups(Long permissionId, GenericValue project);

    /**
     * @deprecated Use {@link #getUsers(Long, com.atlassian.jira.permission.PermissionContext)} instead.
     */
    @Deprecated
    Collection<User> getUsers(Long permissionId, GenericValue issueOrProject);

    Collection<User> getUsers(Long permissionId, PermissionContext ctx);

    /**
     * Will return all @link Scheme objects that are not currently associated with any projects.
     *
     * @return list of @link Scheme objects
     * @throws DataAccessException if the database is down or equivalent.
     */
    List<Scheme> getUnassociatedSchemes() throws DataAccessException;

    /**
     * Gets a scheme by id from the database.
     * @param id the id of the scheme to get.
     * @return the Scheme
     * @throws DataAccessException if the database is down or equivalent.
     */
    Scheme getSchemeObject(Long id) throws DataAccessException;

    /**
     * Gets a scheme by name from the database.
     * @param name the name of the scheme to get.
     * @return the Scheme
     * @throws DataAccessException if the database is down or equivalent.
     */
    Scheme getSchemeObject(String name) throws DataAccessException;

    /**
     * Gets all scheme objects in the database.
     * @return the schemes.
     * @throws DataAccessException if the database is down or equivalent.
     */
    List<Scheme> getSchemeObjects() throws DataAccessException;
}
