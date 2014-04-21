/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.action.setup.SetupOldUserHelper;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericDelegator;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.List;

public class UpgradeTask_Build60 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build60.class);

    public String getBuildNumber()
    {
        return "60";
    }

    public String getShortDescription()
    {
        return "Adding colors to Priorities so the status bar appearence can be configured. Adding View Control Permission to '" + AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS + "' group.";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        log.debug("UpgradeTask_Build60.doUpgrade");

        // Try to get 'jira-developers' group

        if (SetupOldUserHelper.groupExists(AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS))
        {
            GenericValue scheme = ManagerFactory.getPermissionSchemeManager().getDefaultScheme();
            ManagerFactory.getPermissionManager().addPermission(Permissions.VIEW_VERSION_CONTROL, scheme, AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS, GroupDropdown.DESC);
            log.info("Added 'View Version Control' permission to the '" + AbstractSetupAction.DEFAULT_GROUP_DEVELOPERS + "' group for the '" + scheme.getString("name") + "'.");
        }

        GenericDelegator genericDelegator = CoreFactory.getGenericDelegator();

        // List to store
        List toStore = new ArrayList();

        // Loop over the priorites and update them as they where done in the Dashboad action
        List priorities = genericDelegator.findAll("Priority");
        for (int i = 0; i < priorities.size(); i++)
        {
            GenericValue priority = (GenericValue) priorities.get(i);
            priority.set("statusColor", getColour(priority.getString("id")));
            toStore.add(priority);
        }
        genericDelegator.storeAll(toStore);
    }

    private String getColour(String id)
    {
        if (id == null)
        {
            return "#cccccc";
        }
        else
        {
            if (id.equals("5"))
                return "#003300";
            else if (id.equals("4"))
                return "#006600";
            else if (id.equals("3"))
                return "#009900";
            else if (id.equals("2"))
                return "#ff0000";
            else
                return "#cc0000";
        }
    }
}
