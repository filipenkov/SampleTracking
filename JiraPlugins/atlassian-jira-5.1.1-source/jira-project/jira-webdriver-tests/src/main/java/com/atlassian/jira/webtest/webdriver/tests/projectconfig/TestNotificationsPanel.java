package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.project.ProjectSharedBy;
import com.atlassian.jira.pageobjects.project.notifications.Notification;
import com.atlassian.jira.pageobjects.project.notifications.NotificationsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.notifications.NotificationsPanel;
import com.atlassian.jira.pageobjects.project.summary.notifications.ProjectEmailDialog;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the notifications panel.
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@Restore ("xml/TestProjectConfigNotificationsTab.xml")
public class TestNotificationsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_WITH_NO_NOTIFICATIONS = "MKY";
    private static final String PROJECT_WITH_NOTIFICATIONS = "HSP";
    private static final String FOO_DOO_COM = "foo@doo.com";
    private static final String SCOTT_BONDI_COM = "scott@bondi.com";
    private static final String EXPECTED_DEFAULT_EMAIL = "Expected project email to be [" + FOO_DOO_COM + "]";

    @Test
    public void testNoNotification()
    {
        NotificationsPage notificationsPage = jira.gotoLoginPage().loginAsSysAdmin(NotificationsPage.class, PROJECT_WITH_NO_NOTIFICATIONS);
        assertFalse(notificationsPage.isSchemeLinked());
        assertTrue(notificationsPage.isSchemeChangeAvailable());
        assertFalse(notificationsPage.hasNotificationScheme());
        
        assertTrue(notificationsPage.getNotifications().isEmpty());
        assertTrue(notificationsPage.isNotificationsMessagePresent());
        assertFalse(notificationsPage.getSharedBy().isPresent());
    }

    @Test
    public void testWithNotifications()
    {
        NotificationsPage notificationsPage = jira.gotoLoginPage().loginAsSysAdmin(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        // Assert the edit and change links in the header
        assertTrue(notificationsPage.isSchemeLinked());
        assertTrue(notificationsPage.isSchemeChangeAvailable());
        assertEquals("HSP Notification Scheme", notificationsPage.getNotificationSchemeName());

        // Assert description
        assertEquals("This is a test scheme", notificationsPage.getDesc());

        // Assert mail server message
        assertFalse(notificationsPage.hasMailServer());
        assertTrue(notificationsPage.canChangeMailServer());

        List<Notification> notifications = notificationsPage.getNotifications();

        // There should be one notification for each possible event
        assertEquals(16, notifications.size());
        // Check some specially set up ones and some standard ones.
        assertNotification(notifications, "Generic Event", Collections.<String>emptyList());
        assertNotification(notifications, "Issue Created", Arrays.asList("All Watchers", "Current Assignee", "Reporter"));
        assertNotification(notifications, "Issue Updated", Arrays.asList("Single User (fred)"));
        assertNotification(notifications, "Issue Assigned", Arrays.asList( "Group (jira-developers)", "Single User (fred)"));
        assertNotification(notifications, "Issue Resolved", Arrays.asList("Reporter"));

        assertFalse(notificationsPage.isNotificationsMessagePresent());

        final ProjectSharedBy sharedBy = notificationsPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Shared Notifications", "homosapien"), sharedBy.getProjects());
    }

    @Test
    public void testProjectEmail()
    {
        NotificationsPage notificationsPage = jira.gotoLoginPage().loginAsSysAdmin(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        assertFalse("Expected no project email as mail server has not been configured",
                notificationsPage.hasProjectEmail());

        assertFalse("Expected message to configure mail server",
                notificationsPage.hasMailServer());

        notificationsPage = notificationsPage.configureMailServer()
                .configureNewSTMP()
                .fill("My Server", FOO_DOO_COM, "My Email", "mail.atlassian.com")
                .submit(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        assertTrue("Expected project email to be configured",
                notificationsPage.hasProjectEmail());

        assertEquals(FOO_DOO_COM, notificationsPage.getProjectEmail());

        ProjectEmailDialog emailDialog =  notificationsPage.openProjectEmailDialog()
                .setFromAddress("fdsgfds")
                .submit();

        assertNotNull("Expected error message for invalid email address",
                emailDialog.getError());

        notificationsPage = emailDialog.setFromAddress(SCOTT_BONDI_COM)
                .submit(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        assertEquals(SCOTT_BONDI_COM, notificationsPage.getProjectEmail());

        emailDialog = notificationsPage.openProjectEmailDialog();

        assertEquals(EXPECTED_DEFAULT_EMAIL, SCOTT_BONDI_COM, emailDialog.getFromAddressValue());

        // clear should go back to default
        assertTrue(emailDialog.setFromAddress("").submit(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS)
                .getProjectEmail().equals(FOO_DOO_COM));
    }

    @Test
    public void testNotAdminNotifications()
    {
        NotificationsPage notificationsPage = jira.gotoLoginPage().login("fred", "fred", NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        // Assert the cog actions aren't present
        assertFalse(notificationsPage.isSchemeLinked());
        assertFalse(notificationsPage.isSchemeChangeAvailable());

        assertEquals("HSP Notification Scheme",  notificationsPage.getNotificationSchemeName());

        // Assert description
        assertEquals("This is a test scheme", notificationsPage.getDesc());

        // Assert mail server message
        assertFalse(notificationsPage.hasMailServer());
        assertFalse(notificationsPage.canChangeMailServer());

        List<Notification> notifications = notificationsPage.getNotifications();

        // There should be one notification for each possible event
        assertEquals(16, notifications.size());
        // Check some specially set up ones and some standard ones.
        assertNotification(notifications, "Generic Event", Collections.<String>emptyList());
        assertNotification(notifications, "Issue Created", Arrays.asList("All Watchers", "Current Assignee", "Reporter"));
        assertNotification(notifications, "Issue Updated", Arrays.asList("Single User (fred)"));
        assertNotification(notifications, "Issue Assigned", Arrays.asList( "Group (jira-developers)", "Single User (fred)"));
        assertNotification(notifications, "Issue Resolved", Arrays.asList("Reporter"));

        assertFalse(notificationsPage.isNotificationsMessagePresent());
        assertFalse(notificationsPage.getSharedBy().isPresent());
    }

    @Test
    public void testNotAdminCanViewSharedBy()
    {
        NotificationsPage notificationsPage = jira.gotoLoginPage().login("fred", "fred", NotificationsPage.class, "OTHERSHARED");

        final ProjectSharedBy sharedBy = notificationsPage.getSharedBy();
        assertTrue(sharedBy.isPresent());
        assertEquals("2 projects", sharedBy.getTriggerText());
        assertEquals(Arrays.asList("Another Shared", "Some other shared"), sharedBy.getProjects());
    }

    @Test
    public void testWithMailServer()
    {
        jira.gotoLoginPage().loginAsSysAdmin(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        setupMailServer();

        NotificationsPage notificationsPage = jira.gotoLoginPage().loginAsSysAdmin(NotificationsPage.class, PROJECT_WITH_NOTIFICATIONS);

        // Assert the edit and change links in the header
        assertTrue(notificationsPage.isSchemeLinked());
        assertTrue(notificationsPage.isSchemeChangeAvailable());
        assertEquals("HSP Notification Scheme", notificationsPage.getNotificationSchemeName());

        // Assert description
        assertEquals("This is a test scheme", notificationsPage.getDesc());

        // Assert mail server message
        assertTrue(notificationsPage.hasMailServer());

        List<Notification> notifications = notificationsPage.getNotifications();

        // There should be one notification for each possible event
        assertEquals(16, notifications.size());
        // Check some specially set up ones and some standard ones.
        assertNotification(notifications, "Generic Event", Collections.<String>emptyList());
        assertNotification(notifications, "Issue Created", Arrays.asList("All Watchers", "Current Assignee", "Reporter"));
        assertNotification(notifications, "Issue Updated", Arrays.asList("Single User (fred)"));
        assertNotification(notifications, "Issue Assigned", Arrays.asList( "Group (jira-developers)", "Single User (fred)"));
        assertNotification(notifications, "Issue Resolved", Arrays.asList("Reporter"));

        assertFalse(notificationsPage.isNotificationsMessagePresent());
    }

    private void setupMailServer()
    {
        NotificationsPanel notificationsPanel = navigateToNotificationsPanelFor(PROJECT_WITH_NOTIFICATIONS);

        assertFalse("Expected no project email as mail server has not been configured",
                notificationsPanel.hasProjectEmail());

        assertFalse("Expected message to configure mail server",
                notificationsPanel.hasServerConfiguration());

        configureMailServer(notificationsPanel);
    }

    private NotificationsPanel navigateToNotificationsPanelFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey).openPanel(NotificationsPanel.class);
    }
    
    private NotificationsPanel configureMailServer(NotificationsPanel notificationsPanel)
    {
        return notificationsPanel.configureMailServer()
                .configureNewSTMP()
                .fill("My Server", FOO_DOO_COM, "My Email", "mail.atlassian.com")
                .submit(ProjectSummaryPageTab.class, PROJECT_WITH_NOTIFICATIONS)
                .openPanel(NotificationsPanel.class);
    }

    private void assertNotification(List<Notification> notifications, String eventName, List<String> objects)
    {
        // Find the entry in the list
        for (Notification notification : notifications)
        {
            if (notification.getName().equals(eventName))
            {
                // assert the list is the same as that provided
                assertEquals("Notification entities did not match for :" + notification.getName(), objects, notification.getEntities());
                return;
            }
        }
        fail("Expected notification '" + eventName + "' not found.");
    }
}
