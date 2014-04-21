package com.atlassian.jira.upgrade.tasks;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import org.ofbiz.core.entity.GenericValue;

/**
 * @since v4.0
 */
public class TestUpgradeTask_Build35 extends AbstractUsersTestCase
{
    public TestUpgradeTask_Build35(String string)
    {
        super(string);
    }

    public void testDefaultNotificationScheme() throws Exception
    {
        com.atlassian.jira.upgrade.tasks.UpgradeTask_Build35 upgrade = new com.atlassian.jira.upgrade.tasks.UpgradeTask_Build35();
        upgrade.doUpgrade(false);

        NotificationSchemeManager schemeManager = ManagerFactory.getNotificationSchemeManager();

        final GenericValue defaultScheme = schemeManager.getScheme("Default Notification Scheme");
        assertNotNull(defaultScheme);        
        assertNull(defaultScheme.getString("description"));
    }
}
