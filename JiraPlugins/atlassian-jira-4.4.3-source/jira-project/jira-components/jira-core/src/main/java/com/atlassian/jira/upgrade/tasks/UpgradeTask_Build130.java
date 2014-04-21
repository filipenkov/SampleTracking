/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.GlobalPermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class UpgradeTask_Build130 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build130.class);
    private final OfBizDelegator delegator;

    public UpgradeTask_Build130(OfBizDelegator delegator)
    {
        this.delegator = delegator;
    }

    public String getBuildNumber()
    {
        return "130";
    }

    public String getShortDescription()
    {
        return "Grant the global 'Bulk Change' permission to all groups that have the global 'JIRA Users' permission.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        GlobalPermissionManager globalPermissionManager = ManagerFactory.getGlobalPermissionManager();
        for (String groupName : getGroupNamesWithUsePermission())
        {
            globalPermissionManager.addPermission(Permissions.BULK_CHANGE, groupName != null ? groupName : null);
        }
    }
    
    Collection<String> getGroupNamesWithUsePermission()
    {
        final Collection<String> groupNames = new ArrayList<String>();
        // Get all the Global permissions (indicated by scheme == null) that are of the type USE
        final List<GenericValue> globalPermissionsOfType = delegator.findByAnd("SchemePermissions", EasyMap.build("scheme", null, "permission", Permissions.USE, "type", "group"));
        for (GenericValue genericValue : globalPermissionsOfType)
        {
            // The parameter is always the group primary key, in this case the group name
            groupNames.add(genericValue.getString("parameter"));
        }
        return groupNames;
    }
}
