/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.security.util;

import com.atlassian.jira.permission.PermissionSchemeManager;
import com.atlassian.jira.permission.SchemePermissions;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class GroupToPermissionSchemeMapper extends AbstractGroupToSchemeMapper
{
    private final SchemePermissions schemePermissions;

    public GroupToPermissionSchemeMapper(PermissionSchemeManager permissionSchemeManager, SchemePermissions schemePermissions) throws GenericEntityException
    {
        super(permissionSchemeManager);
        this.schemePermissions = schemePermissions;
        setGroupMapping(realInit());
    }

    /** Nasty hacks to get around the dependencies problem **/
    protected Map init() throws GenericEntityException
    {
        return null;
    }

    /**
     * Go through all the Permission Schemes and create a Map, where the key is the group name
     * and values are Sets of Schemes
     */
    protected Map realInit() throws GenericEntityException
    {
        Map mapping = new HashMap();

        // Get all Permission Schmes
        final List schemes = getSchemeManager().getSchemes();
        for (int i = 0; i < schemes.size(); i++)
        {
            GenericValue permissionScheme = (GenericValue) schemes.get(i);

            // For each scheme get all the permissions
            final Map allSchemePermissions = schemePermissions.getSchemePermissions();
            for (Iterator iterator = allSchemePermissions.keySet().iterator(); iterator.hasNext();)
            {
                Integer permissionId = (Integer) iterator.next();

                // Get all the groups for this permission
                final List entities = getSchemeManager().getEntities(permissionScheme, new Long(permissionId.longValue()));
                for (int j = 0; j < entities.size(); j++)
                {
                    GenericValue permissionRecord = (GenericValue) entities.get(j);
                    if ("group".equals(permissionRecord.getString("type")))
                    {
                        addEntry(mapping, permissionRecord.getString("parameter"), permissionScheme);
                    }
                }
            }
        }

        return mapping;
    }
}
