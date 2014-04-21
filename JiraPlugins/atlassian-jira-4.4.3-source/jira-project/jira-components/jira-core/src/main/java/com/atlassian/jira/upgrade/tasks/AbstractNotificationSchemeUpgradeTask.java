package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.upgrade.AbstractUpgradeTask;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Iterator;
import java.util.List;

/**
 * This upgrade task adds new scheme entities to the eventIdDestination of all notification schemes in JIRA. It uses
 * the eventIdSource scheme entities as a template for what to create.
 *
 * For example UpgradeTask_Build207 uses this to grant the Comment Edited notification event to all entities who
 * currently have the Issue Commented event.
 */
public abstract class AbstractNotificationSchemeUpgradeTask extends AbstractUpgradeTask
{
    private static final Logger LOG = Logger.getLogger(UpgradeTask_Build207.class);

    private final NotificationSchemeManager notificationSchemeManager;

    public AbstractNotificationSchemeUpgradeTask(NotificationSchemeManager notificationSchemeManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
    }

    public void doUpgrade(Long eventIdSource, Long eventIdDestination) throws GenericEntityException
    {
        LOG.info("About to add edit comment notifications to all notification schemes...");
        try
        {
            final List /* <GenericValue> */ schemes = notificationSchemeManager.getSchemes();
            for (Iterator i = schemes.iterator(); i.hasNext();)
            {
                GenericValue schemeGV = (GenericValue) i.next();

                // Copy all the scheme entities registered for the passed in event
                List entities = notificationSchemeManager.getEntities(schemeGV, eventIdSource);
                for (Iterator iterator = entities.iterator(); iterator.hasNext();)
                {
                    GenericValue schemeEntity = (GenericValue) iterator.next();
                    addSchemeEntityForDestinationNotification(schemeGV, schemeEntity, eventIdDestination);
                }
            }
            LOG.info("Done adding edit comment notifications to all notification schemes.");
        }
        catch (GenericEntityException e)
        {
            LOG.error("Unable to retrieve all notification schemes.", e);
            throw e;
        }
    }

    private void addSchemeEntityForDestinationNotification(GenericValue schemeGV, GenericValue origSchemeEntity, Long eventIdDestination)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("About to add notification for event id: '" + eventIdDestination + "' for '" + origSchemeEntity.getString("type") + "' to notification scheme '"
                    + schemeGV.getString("name") + "'");
        }

        SchemeEntity schemeEntity = new SchemeEntity(origSchemeEntity.getString("type"),origSchemeEntity.getString("parameter") , eventIdDestination);
        try
        {
            notificationSchemeManager.createSchemeEntity(schemeGV, schemeEntity);
        }
        catch (GenericEntityException e)
        {
            LOG.error("Failed to add notification for event id: '"+ eventIdDestination + "' for '" + schemeEntity + "' to notification scheme '"
                    + schemeGV.getString("name") + "'!");
        }
    }
}
