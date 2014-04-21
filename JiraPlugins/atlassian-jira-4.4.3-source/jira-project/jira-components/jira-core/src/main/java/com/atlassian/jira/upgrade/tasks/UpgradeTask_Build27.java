/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

public class UpgradeTask_Build27 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build27.class);

    public String getBuildNumber()
    {
        return "27";
    }

    /**
     * This upgrade task that creates and fills the new status entries
     * @param setupMode
     */
    public void doUpgrade(boolean setupMode)
    {
        log.debug("UpgradeTask_Build27 - fill new Status entity table.");

        try
        {
            UpgradeTask_Build11.createNewEntity("Status", 1, "Unassigned", "The issue is not assigned to anyone yet. It can be assigned from here.", "/images/icons/status_unassigned.gif");
            UpgradeTask_Build11.createNewEntity("Status", 2, "Assigned", "This issue has recently been added to the assignee's list of issues and must be processed. Issues in this state may be assigned to someone else, or resolved and marked resolved.", "/images/icons/status_assigned.gif");
            UpgradeTask_Build11.createNewEntity("Status", 3, "In Progress", "This issue is being actively worked on at the moment by the assignee.", "/images/icons/status_inprogress.gif");
            UpgradeTask_Build11.createNewEntity("Status", 4, "Reopened", "This issue was once resolved, but the resolution was deemed incorrect. From here issues are either marked assigned or resolved.", "/images/icons/status_reopened.gif");
            UpgradeTask_Build11.createNewEntity("Status", 5, "Resolved", "A resolution has been taken, and it is awaiting verification by reporter. From here issues are either reopened, or are closed.", "/images/icons/status_resolved.gif");
            UpgradeTask_Build11.createNewEntity("Status", 6, "Closed", "The issue is considered finished, the resolution is correct. Issues which are closed can be reopened.", "/images/icons/status_closed.gif");
        }
        catch (GenericEntityException e)
        {
            log.error("Error adding resolution: " + e, e);
            addError(getI18nBean().getText("admin.errors.error.adding.resolution"));
        }
    }
}
