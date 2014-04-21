package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.admin.SelectIssueSecurityScheme;
import com.atlassian.jira.pageobjects.project.issuesecurity.IssueSecurityPage;
import com.atlassian.jira.pageobjects.project.permissions.ProjectPermissionPageTab;
import com.atlassian.jira.pageobjects.project.summary.PermissionsPanel;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE  })
@RestoreOnce ("xml/SummaryPermissionsPanel.xml")
public class TestSummaryPermissionsPanel extends BaseJiraWebTest
{
    private static final String PROJECT_WITH_PERMISSION_SCHEME = "HSP";
    private static final String PROJECT_NO_PERMISSION_SCHEME = "MKY";

    @Test
    public void testProjectWithIssueLevelSecurity()
    {
        String permissionScheme = "<b>XSS</b>";
        String expectedIssueSecurity = "<b>XSS Security Scheme</b>";

        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_WITH_PERMISSION_SCHEME);

        summaryPage = assertEditPermissionsScheme(permissionScheme, summaryPage);
        PermissionsPanel panel = summaryPage.openPanel(PermissionsPanel.class);

        assertEquals(expectedIssueSecurity, panel.getIssueSecurityScheme());

        IssueSecurityPage issueSecurityPage = panel.gotoIssueSecurityPage();
        assertEquals(expectedIssueSecurity, issueSecurityPage.getSchemeName().getText());

    }

    @Test
    public void testProjectWithIssueLevelSecurityProjectAdmin()
    {
        String expectedIssueSecurity = "<b>XSS Security Scheme</b>";


        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .login("fred", "fred", ProjectSummaryPageTab.class, PROJECT_WITH_PERMISSION_SCHEME);


        PermissionsPanel panel = summaryPage.openPanel(PermissionsPanel.class);
        assertEquals(expectedIssueSecurity, panel.getIssueSecurityScheme());
    }

    @Test
    public void testProjectWithoutIssueLevelSecurity()
    {
        String expectedPermissionScheme = "Default Permission Scheme";

        ProjectSummaryPageTab summaryPage = jira.gotoLoginPage()
                .loginAsSysAdmin(ProjectSummaryPageTab.class, PROJECT_NO_PERMISSION_SCHEME);

        summaryPage = assertEditPermissionsScheme(expectedPermissionScheme, summaryPage);
        PermissionsPanel panel = summaryPage.openPanel(PermissionsPanel.class);

        IssueSecurityPage issueSecurityPage = panel.gotoIssueSecurityPage();
        assertEquals("Anyone", issueSecurityPage.getSchemeName().getText());
    }

    private ProjectSummaryPageTab assertEditPermissionsScheme(String permissionScheme,
            ProjectSummaryPageTab summaryPage)
    {
        PermissionsPanel permissions = summaryPage.openPanel(PermissionsPanel.class);

        assertTrue(permissions.isCanEditPermissionScheme());
        assertEquals(permissionScheme, permissions.getPermissionScheme());

        //Just make sure we goto the right page.
        ProjectPermissionPageTab editScheme = permissions.gotoPermissionsPage();

        summaryPage = editScheme.back(ProjectSummaryPageTab.class, summaryPage.getProjectKey());
        permissions = summaryPage.openPanel(PermissionsPanel.class);

        return summaryPage;
    }

}
