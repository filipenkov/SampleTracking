package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.event.type.EventType;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Iterator;

/**
 *
 */
public class TestUpgradeTask_Build258 extends LegacyJiraMockTestCase
{
    private NotificationSchemeManager notificationSchemeManager;

    protected void setUp() throws Exception
    {
        super.setUp();

        // Get a hold of the notification scheme manager
        notificationSchemeManager = (NotificationSchemeManager) ComponentManager.getComponentInstanceOfType(NotificationSchemeManager.class);
    }

    public void testDoUpgradeWithStandardNotificationScheme() throws Exception
    {
        // Running the upgrade task will ensure that we have a real
        // representation of the default notification scheme so that we can
        // exercise all the possible strange bits in a scheme. This one tests
        // scheme entities with null parameters
        UpgradeTask_Build35 upgradeTask_build35 = new UpgradeTask_Build35();
        upgradeTask_build35.doUpgrade(false);

        // create two more schemes - copies of default notification scheme
        GenericValue defaultNotificationScheme = notificationSchemeManager.getScheme("Default Notification Scheme");
        notificationSchemeManager.copyScheme(defaultNotificationScheme);
        notificationSchemeManager.copyScheme(defaultNotificationScheme);

        // run the upgrade task
        UpgradeTask_Build258 upgradeTask_build258 = new UpgradeTask_Build258(notificationSchemeManager);
        upgradeTask_build258.doUpgrade(false);

        // verify that all schemes were updated
        final List schemes = notificationSchemeManager.getSchemes();

        assertEquals(3, schemes.size());

        for (Iterator i = schemes.iterator(); i.hasNext();)
        {
            GenericValue schemeGV = (GenericValue) i.next();
            final List worklogUpdatedEntities = notificationSchemeManager.getEntities(schemeGV, EventType.ISSUE_WORKLOG_UPDATED_ID);
            assertNotNull(worklogUpdatedEntities);
            assertEquals(3, worklogUpdatedEntities.size());
            assertSchemeEntityListContainsType("All_Watchers", worklogUpdatedEntities);
            assertSchemeEntityListContainsType("Current_Reporter", worklogUpdatedEntities);
            assertSchemeEntityListContainsType("Current_Assignee", worklogUpdatedEntities);
            final List worklogDeletedEntities = notificationSchemeManager.getEntities(schemeGV, EventType.ISSUE_WORKLOG_DELETED_ID);
            assertNotNull(worklogDeletedEntities);
            assertEquals(3, worklogDeletedEntities.size());
            assertSchemeEntityListContainsType("All_Watchers", worklogDeletedEntities);
            assertSchemeEntityListContainsType("Current_Reporter", worklogDeletedEntities);
            assertSchemeEntityListContainsType("Current_Assignee", worklogDeletedEntities);
        }

    }

    public void testDoUpgradeWithCustomNotificationScheme() throws Exception
    {
        // Running the upgrade task will ensure that we have a real
        // representation of the default notification scheme so that we can
        // exercise all the possible strange bits in a scheme.
        UpgradeTask_Build35 upgradeTask_build35 = new UpgradeTask_Build35();
        upgradeTask_build35.doUpgrade(false);

        // Get the default notification scheme and remove all ISSUE_COMMENTED notificiees
        GenericValue defaultNotificationScheme = notificationSchemeManager.getScheme("Default Notification Scheme");
        notificationSchemeManager.removeEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOGGED_ID);
        final String type = "testtype";
        final String param = "testparameter";
        // This is testing schemeEntities with non-null parameters
        notificationSchemeManager.createSchemeEntity(defaultNotificationScheme, new SchemeEntity(type, param, EventType.ISSUE_WORKLOGGED_ID));

        // run the upgrade task
        UpgradeTask_Build258 upgradeTask_build258 = new UpgradeTask_Build258(notificationSchemeManager);
        upgradeTask_build258.doUpgrade(false);

        // verify that the notification schemes was updated by adding the custom scheme entitye with with the correct event type
        final List worklogUpdatedEntities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOG_UPDATED_ID);
        assertEquals(1, worklogUpdatedEntities.size());
        GenericValue updatedSchemeEntity = (GenericValue) worklogUpdatedEntities.get(0);
        assertEquals(type, updatedSchemeEntity.getString("type"));
        assertEquals(param, updatedSchemeEntity.getString("parameter"));
        final List worklogDeletedEntities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOG_DELETED_ID);
        assertEquals(1, worklogDeletedEntities.size());
        GenericValue deletedSchemeEntity = (GenericValue) worklogDeletedEntities.get(0);
        assertEquals(type, deletedSchemeEntity.getString("type"));
        assertEquals(param, deletedSchemeEntity.getString("parameter"));
    }

    public void testDoUpgradeWithEmptyNotificationScheme() throws Exception
    {
        // Running the upgrade task will ensure that we have a real
        // representation of the default notification scheme so that we can
        // exercise all the possible strange bits in a scheme.
        UpgradeTask_Build35 upgradeTask_build35 = new UpgradeTask_Build35();
        upgradeTask_build35.doUpgrade(false);

        // Get the default notification scheme and remove all ISSUE_COMMENTED notificiees
        GenericValue defaultNotificationScheme = notificationSchemeManager.getScheme("Default Notification Scheme");
        notificationSchemeManager.removeEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOGGED_ID);

        // run the upgrade task
        UpgradeTask_Build258 upgradeTask_build258 = new UpgradeTask_Build258(notificationSchemeManager);
        upgradeTask_build258.doUpgrade(false);

        // verify that the notification schemes was updated by adding no new scheme entities

        final List worklogUpdatedEntities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOG_UPDATED_ID);
        assertEquals(0, worklogUpdatedEntities.size());
        final List worklogDeletedEntities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_WORKLOG_DELETED_ID);
        assertEquals(0, worklogDeletedEntities.size());
    }

    public void assertSchemeEntityListContainsType(String type, List entities)
    {
        for (Iterator i = entities.iterator(); i.hasNext();)
        {
            GenericValue entity = (GenericValue) i.next();
            final String entityType = entity.getString("type");
            if (entityType.equals(type))
            {
                return;
            }
        }
        fail("List of scheme entities does not contain an entity with '" + type + "' type");
    }

}
