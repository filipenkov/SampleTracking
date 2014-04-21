package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.pageobjects.project.summary.notifications.NotificationsPanel;
import com.atlassian.jira.pageobjects.project.summary.notifications.ProjectEmailDialog;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */

@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@Restore ("xml/TestProjectConfigSummaryNotificationsPanel.xml")
public class TestSummaryNotificationsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_ALL = "HSP";
    private static final String PROJECT_DEFAULT = "MKY";
    private static final String DEFAULT_NOTIFICATION_SCHEME = "Default Notification Scheme";
    private static final String NONE = "None";
    private static final String FOO_DOO_COM = "foo@doo.com";
    private static final String SCOTT_BONDI_COM = "scott@bondi.com";
    private static final String EXPECTED_DEFAULT_EMAIL = "Expected project email to be [" + FOO_DOO_COM + "]";

    @Test
    public void testNotificationScheme()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        NotificationsPanel notificationsPanel = navigateToNotificationsPanelFor(PROJECT_ALL);

        assertEquals(NONE, notificationsPanel.getNotificationSchemeLinkText());
        assertEquals(createNotificiationsUrl(PROJECT_ALL), notificationsPanel.getNotificationSchemeLinkUrl());

        notificationsPanel = navigateToNotificationsPanelFor(PROJECT_DEFAULT);

        assertEquals(DEFAULT_NOTIFICATION_SCHEME, notificationsPanel.getNotificationSchemeLinkText());
        assertEquals(createNotificiationsUrl(PROJECT_DEFAULT), notificationsPanel.getNotificationSchemeLinkUrl());
    }

    @Test
    public void testProjectEmail()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        NotificationsPanel notificationsPanel = navigateToNotificationsPanelFor(PROJECT_ALL);

        assertFalse("Expected no project email as mail server has not been configured",
                notificationsPanel.hasProjectEmail());

        assertFalse("Expected message to configure mail server",
                notificationsPanel.hasServerConfiguration());

        notificationsPanel = configureMailServer(notificationsPanel);

        assertTrue("Expected project email to be configured",
                notificationsPanel.hasProjectEmail());

        assertEquals(FOO_DOO_COM, notificationsPanel.getProjectEmail());


        ProjectEmailDialog emailDialog =  notificationsPanel.openProjectEmailDialog()
                .setFromAddress("fdsgfds")
                .submit();

        assertNotNull("Expected error message for invalid email address",
                emailDialog.getError());


        notificationsPanel = emailDialog.setFromAddress(SCOTT_BONDI_COM)
                .submit(ProjectSummaryPageTab.class, PROJECT_ALL)
                .openPanel(NotificationsPanel.class);

        waitUntilEquals(SCOTT_BONDI_COM, notificationsPanel.projectEmailHolder().timed().getText());

        emailDialog = notificationsPanel.openProjectEmailDialog();

        assertEquals(EXPECTED_DEFAULT_EMAIL, SCOTT_BONDI_COM, emailDialog.getFromAddressValue());

        // clear should go back to default
        assertTrue(emailDialog.setFromAddress("").submit(ProjectSummaryPageTab.class, PROJECT_ALL)
                .openPanel(NotificationsPanel.class)
                .getProjectEmail().equals(FOO_DOO_COM));
    }

    private NotificationsPanel configureMailServer(NotificationsPanel notificationsPanel)
    {
        return notificationsPanel.configureMailServer()
                .configureNewSTMP()
                .fill("My Server", FOO_DOO_COM, "My Email", "mail.atlassian.com")
                .submit(ProjectSummaryPageTab.class, PROJECT_ALL)
                .openPanel(NotificationsPanel.class);
    }

    @Test
    public void testPermissions()
    {
        jira.gotoLoginPage().login("proadmin", "proadmin", DashboardPage.class);

        NotificationsPanel notificationsPanel = navigateToNotificationsPanelFor(PROJECT_DEFAULT);

        assertFalse("Expected user NOT to have configure mail permissions", notificationsPanel.hasConfigureMailPermissions());

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        notificationsPanel = navigateToNotificationsPanelFor(PROJECT_DEFAULT);

        assertTrue("Expected user to have configure mailserver permissions", notificationsPanel.hasConfigureMailPermissions());

        configureMailServer(notificationsPanel);

        jira.gotoLoginPage().login("proadmin", "proadmin", DashboardPage.class);

        notificationsPanel =  navigateToNotificationsPanelFor(PROJECT_DEFAULT);

        assertFalse("Expected user NOT to have edit email permissions", notificationsPanel.hasEditEmailPermissions());

        jira.gotoLoginPage().login("proadmin", "proadmin", DashboardPage.class);

        notificationsPanel =  navigateToNotificationsPanelFor(PROJECT_ALL);

        assertFalse("Expected user NOT to have edit email permissions", notificationsPanel.hasEditEmailPermissions());

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        notificationsPanel = navigateToNotificationsPanelFor(PROJECT_ALL);

        assertTrue("Expected user to have edit email permissions", notificationsPanel.hasEditEmailPermissions());

    }


    private String createNotificiationsUrl(String projectKey)
    {
        return jira.getProductInstance().getContextPath() + "/plugins/servlet/project-config/" + projectKey + "/notifications";
    }

    private NotificationsPanel navigateToNotificationsPanelFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey).openPanel(NotificationsPanel.class);
    }

}
