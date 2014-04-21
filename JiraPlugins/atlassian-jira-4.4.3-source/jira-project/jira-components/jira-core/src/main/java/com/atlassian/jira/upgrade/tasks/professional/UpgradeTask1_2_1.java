package com.atlassian.jira.upgrade.tasks.professional;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import com.atlassian.jira.web.action.setup.AbstractSetupAction;
import com.atlassian.jira.web.action.setup.SetupOldUserHelper;
import org.apache.log4j.Logger;

public class UpgradeTask1_2_1 extends AbstractUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask1_2_1.class);

    public String getBuildNumber()
    {
        // JRADEV-6192: Originally this returned "1.2", but was altered to make all build numbers simple integers.
        return "2";
    }

    public void doUpgrade(boolean setupMode)
    {
        /* SETUP DEFAULT PERMISSSIONS FOR NEW PERMISSIONS SECTION - 'CREATE SHARED FILTER' */
        log.debug("UpgradeTask1_2_1 - setting up default permissions for 'CREATE SHARED FILTER'");

        try
        {
            SetupOldUserHelper.addGroup(AbstractSetupAction.DEFAULT_GROUP_USERS);
            ManagerFactory.getGlobalPermissionManager().addPermission(Permissions.CREATE_SHARED_OBJECTS, AbstractSetupAction.DEFAULT_GROUP_USERS);
        }
        catch (Exception e)
        {
            addError(getI18nBean().getText("admin.errors.exception") + " " + e);
            log.error("Exception: " + e);
        }
    }
}
