/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.permission;

import com.atlassian.jira.scheme.SchemeManager;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * This class is used to handle Permission Schemes. Permission Schemes are created, removed and edited through this class
 */
public interface PermissionSchemeManager extends SchemeManager
{
    public String getSchemeEntityName();

    public String getEntityName();

    public String getAssociationType();

    public String getSchemeDesc();

    /**
     * Get all Generic Value permission records for a particular scheme and permission Id
     * @param scheme The scheme that the permissions belong to
     * @param permissionId The Id of the permission
     * @param parameter The permission parameter (group name etc)
     * @param type The type of the permission(Group, Current Reporter etc)
     * @return List of (GenericValue) permissions
     * @throws GenericEntityException
     */
    public List<GenericValue> getEntities(GenericValue scheme, Long permissionId, String type, String parameter) throws GenericEntityException;

    public void flushSchemeEntities();

    /**
     * This is a method that is meant to quickly get you all the schemes that contain an entity of the
     * specified type and parameter.
     * @param type is the entity type
     * @param parameter is the scheme entries parameter value
     * @return Collection of GenericValues that represents a scheme
     */
    public Collection<GenericValue> getSchemesContainingEntity(String type, String parameter);

}
