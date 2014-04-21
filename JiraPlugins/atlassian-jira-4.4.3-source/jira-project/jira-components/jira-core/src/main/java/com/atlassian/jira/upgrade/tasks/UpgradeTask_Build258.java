package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.NotificationSchemeManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

/**
 *
 */
public class UpgradeTask_Build258 extends AbstractNotificationSchemeUpgradeTask
{
    private static final Logger log = Logger.getLogger(UpgradeTask_Build258.class);

    public UpgradeTask_Build258(NotificationSchemeManager notificationSchemeManager)
    {
        super(notificationSchemeManager);
    }

    public String getShortDescription()
    {
        return "This task updates all notification schemes with update worklog event and delete worklog event notification to be the same as the issue worked notification.";
    }

    public String getBuildNumber()
    {
        return "258";
    }

    public void doUpgrade(boolean setupMode) throws Exception
    {
        try
        {
            log.info("About to add update worklog notifications to notification schemes containing the issue worked notification...");
            doUpgrade(EventType.ISSUE_WORKLOGGED_ID, EventType.ISSUE_WORKLOG_UPDATED_ID);
            log.info("Done adding update worklog notifications to notification schemes containing the issue worked notification.");

            log.info("About to add delete worklog notifications to notification schemes containing the issue worked notification...");
            doUpgrade(EventType.ISSUE_WORKLOGGED_ID, EventType.ISSUE_WORKLOG_DELETED_ID);
            log.info("Done adding delete worklog notifications to notification schemes containing the issue worked notification.");
        }
        catch (GenericEntityException e)
        {
            log.error("Unable to retrieve all notification schemes.", e);
            throw e;
        }
    }
}
