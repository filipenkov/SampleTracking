/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a very basic cache that stores permissions
 *
 * When constructed, or when you call refresh() - it will find and cache all permissions
 */
public class GlobalPermissionsCache
{
    private static final Logger log = Logger.getLogger(GlobalPermissionsCache.class);

    // set of all permissions
    private Set<JiraPermission> permissions;

    // map of permission object to GenericValue
    private Map<JiraPermission, GenericValue> permGVs;

    /**
     * Create a new permissions cache.
     */
    GlobalPermissionsCache()
    {
        log.debug("GlobalPermissionsCache.GlobalPermissionsCache");

        try
        {
            refresh();
        }
        catch (final Throwable t)
        {
            log.error("Exception constructing GlobalPermissionsCache: " + t, t);
        }
    }

    /**
     * Refresh the permissions cache
     *
     * IMPACT: Should perform only one SQL select statement
     */
    public synchronized void refresh()
    {
        permissions = new HashSet<JiraPermission>();
        permGVs = new HashMap<JiraPermission, GenericValue>();

        try
        {
            final Collection<GenericValue> allPermissions = CoreFactory.getGenericDelegator().findByAnd("SchemePermissions",
                MapBuilder.build("scheme", null));
            for (final GenericValue permissionGV : allPermissions)
            {
                final JiraPermission jiraPerm = new JiraPermission(permissionGV);
                boolean added = permissions.add(jiraPerm);
                added = ((permGVs.put(jiraPerm, permissionGV) == null) && added);
                if (!added)
                {
                    log.warn("Could not add permission " + jiraPerm + " - it already existed?");
                }
            }
        }
        catch (final GenericEntityException e)
        {
            log.error("Exception refreshing cache: " + e, e);
        }
    }

    public boolean hasPermission(final JiraPermission jiraPermission)
    {
        return permissions.contains(jiraPermission);
    }

    public GenericValue getPermission(final JiraPermission jiraPermission)
    {
        return permGVs.get(jiraPermission);
    }

    public Set<JiraPermission> getPermissions()
    {
        return permissions;
    }

    /**
     * Get a Collection of permission based on a permissionType
     * @param permissionType must be global permission type
     * @return Collction of Permission objects
     */
    public Collection<JiraPermission> getPermissions(final int permissionType)
    {
        if (!Permissions.isGlobalPermission(permissionType))
        {
            throw new IllegalArgumentException("PermissionType passed must be a global permissions " + permissionType + " is not");
        }

        final List<JiraPermission> matchingPerms = new ArrayList<JiraPermission>();
        for (final JiraPermission perm : permissions)
        {
            if (perm.getType() == permissionType)
            {
                matchingPerms.add(perm);
            }
        }
        return matchingPerms;
    }
}
