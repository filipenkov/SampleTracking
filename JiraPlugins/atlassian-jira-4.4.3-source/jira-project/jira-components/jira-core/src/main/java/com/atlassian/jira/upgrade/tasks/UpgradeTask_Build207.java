package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.NotificationSchemeManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

/**
 * This upgrade task adds new
 * {@link com.atlassian.jira.event.type.EventType#ISSUE_COMMENT_EDITED_ID}
 * event notification for all watchers, reported and assignee for all
 * notification schemes.
 */
public class UpgradeTask_Build207 extends AbstractNotificationSchemeUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build207.class);

    public UpgradeTask_Build207(NotificationSchemeManager notificationSchemeManager)
    {
        super(notificationSchemeManager);
    }

    /**
     * Returns a short description of this upgrade task
     *
     * @return a short description string
     */
    public String getShortDescription()
    {
        return "This task updates all notification schemes with edit comment event notification to be the same as the issue commented notification.";
    }

    /**
     * Returns 207 as string
     *
     * @return 207 as string
     */
    public String getBuildNumber()
    {
        return "207";
    }

    public void doUpgrade(boolean setupMode) throws GenericEntityException
    {
        LOG.info("About to add edit comment notifications to all notification schemes...");
        try
        {
            doUpgrade(EventType.ISSUE_COMMENTED_ID, EventType.ISSUE_COMMENT_EDITED_ID);
            LOG.info("Done adding edit comment notifications to all notification schemes.");
        }
        catch (GenericEntityException e)
        {
            LOG.error("Unable to retrieve all notification schemes.", e);
            throw e;
        }
    }
}
