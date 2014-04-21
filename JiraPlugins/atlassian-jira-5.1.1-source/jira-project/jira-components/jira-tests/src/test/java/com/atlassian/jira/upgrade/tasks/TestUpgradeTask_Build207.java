package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.notification.NotificationSchemeManager;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.util.Iterator;
import java.util.List;

public class TestUpgradeTask_Build207 extends LegacyJiraMockTestCase
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
        UpgradeTask_Build207 upgradeTask_build207 = new UpgradeTask_Build207(notificationSchemeManager);
        upgradeTask_build207.doUpgrade(false);

        // verify that all schemes were updated
        final List schemes = notificationSchemeManager.getSchemes();

        assertEquals(3, schemes.size());

        for (Iterator i = schemes.iterator(); i.hasNext();)
        {
            GenericValue schemeGV = (GenericValue) i.next();
            final List entities = notificationSchemeManager.getEntities(schemeGV, EventType.ISSUE_COMMENT_EDITED_ID);
            assertNotNull(entities);
            assertEquals(3, entities.size());
            assertSchemeEntityListContainsType("All_Watchers", entities);
            assertSchemeEntityListContainsType("Current_Reporter", entities);
            assertSchemeEntityListContainsType("Current_Assignee", entities);
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
        notificationSchemeManager.removeEntities(defaultNotificationScheme, EventType.ISSUE_COMMENTED_ID);
        final String type = "testtype";
        final String param = "testparameter";
        // This is testing schemeEntities with non-null parameters
        notificationSchemeManager.createSchemeEntity(defaultNotificationScheme, new SchemeEntity(type, param, EventType.ISSUE_COMMENTED_ID));

        // run the upgrade task
        UpgradeTask_Build207 upgradeTask_build207 = new UpgradeTask_Build207(notificationSchemeManager);
        upgradeTask_build207.doUpgrade(false);

        // verify that the notification schemes was updated by adding the custom scheme entitye with with the correct event type
        final List entities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_COMMENT_EDITED_ID);
        assertEquals(1, entities.size());
        GenericValue schemeEntity = (GenericValue) entities.get(0);
        assertEquals(type, schemeEntity.getString("type"));
        assertEquals(param, schemeEntity.getString("parameter"));
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
        notificationSchemeManager.removeEntities(defaultNotificationScheme, EventType.ISSUE_COMMENTED_ID);

        // run the upgrade task
        UpgradeTask_Build207 upgradeTask_build207 = new UpgradeTask_Build207(notificationSchemeManager);
        upgradeTask_build207.doUpgrade(false);

        // verify that the notification schemes was updated by adding no new scheme entities

        final List entities = notificationSchemeManager.getEntities(defaultNotificationScheme, EventType.ISSUE_COMMENT_EDITED_ID);
        assertEquals(0, entities.size());
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
