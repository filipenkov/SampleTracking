package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.ProjectConfigActions.ProjectOperations;
import com.atlassian.jira.pageobjects.project.ProjectConfigHeader;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.EditProjectDialog;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import org.junit.Before;
import org.junit.Test;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Web test for the project configuration summary page's pluggable view project
 * operations block.
 *
 * @since v4.4
 */
@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS, Category.IGNITE })
@Restore("xml/TestWebFragment.xml")
public class TestProjectOperations extends BaseJiraWebTest
{
    private static final String PROJECT_HOMOSAP_KEY = "HSP";
    private static final String PROJECT_HOMOSAP = "homosapien";
    private static final String NEW_HOMOSAP_NAME = PROJECT_HOMOSAP + " test";
    private static final String PROJECT_ADMIN = "project_admin";
    private static String baseUrl;

    @Before
    public void setUp()
    {
        baseUrl = jira.getProductInstance().getBaseUrl();
    }

    @Test
    public void testLinkVisibilityOnViewProjectForProjectAdmin()
    {
        jira.gotoLoginPage().login(PROJECT_ADMIN, PROJECT_ADMIN, DashboardPage.class);

        ProjectConfigActions projectConfigActions =
                navigateToSummaryPageFor(PROJECT_HOMOSAP_KEY).openOperations();

        waitUntilTrue(projectConfigActions.isOpen());

        assertEquals(getBrowseProjectUrl(PROJECT_HOMOSAP_KEY),
                projectConfigActions.getUrl(ProjectOperations.BROWSE));
        assertFalse(projectConfigActions.hasItem(ProjectOperations.DELETE));

        EditProjectDialog editProjectDialog = projectConfigActions.click(EditProjectDialog.class, ProjectOperations.EDIT);
        assertTrue(editProjectDialog
                .setProjectName(NEW_HOMOSAP_NAME)
                .submit());

        ProjectConfigHeader header = navigateToSummaryPageFor(PROJECT_HOMOSAP_KEY).getProjectHeader();

        assertEquals(NEW_HOMOSAP_NAME, header.getProjectName());
    }

    @Test
    public void testLinkVisibilityOnViewProjectForSystemAdmin()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        ProjectConfigActions projectConfigActions =
                navigateToSummaryPageFor(PROJECT_HOMOSAP_KEY).openOperations();

        waitUntilTrue(projectConfigActions.isOpen());

        assertTrue(projectConfigActions.hasItem(ProjectOperations.BROWSE));
        assertTrue(projectConfigActions.hasItem(ProjectOperations.DELETE));
        assertTrue(projectConfigActions.hasItem(ProjectOperations.EDIT));

        assertEquals(getBrowseProjectUrl(PROJECT_HOMOSAP_KEY), projectConfigActions.getUrl(ProjectOperations.BROWSE));

        EditProjectDialog editProjectDialog = projectConfigActions.click(EditProjectDialog.class, ProjectOperations.EDIT);
        assertTrue(editProjectDialog
                .setProjectName(NEW_HOMOSAP_NAME)
                .submit());

        ProjectSummaryPageTab projectSummaryPage = navigateToSummaryPageFor(PROJECT_HOMOSAP_KEY);
        assertEquals(NEW_HOMOSAP_NAME, projectSummaryPage.getProjectHeader().getProjectName());

        projectConfigActions = projectSummaryPage.openOperations();

        waitUntilTrue(projectConfigActions.isOpen());

        DeleteProjectPage deleteProjectPage = projectConfigActions.click(DeleteProjectPage.class, ProjectOperations.DELETE);
        deleteProjectPage.submitConfirm();

        ViewProjectsPage viewProjectsPage = pageBinder.bind(ViewProjectsPage.class);
        assertFalse(viewProjectsPage.isRowPresent(PROJECT_HOMOSAP));
    }

    private ProjectSummaryPageTab navigateToSummaryPageFor(final String projectKey)
    {
        return pageBinder.navigateToAndBind(ProjectSummaryPageTab.class, projectKey);
    }

    private String getBrowseProjectUrl(final String projectKey)
    {
        return baseUrl + "/browse/" + projectKey;
    }
}
