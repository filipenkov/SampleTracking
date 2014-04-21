package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BambooRelease;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowseVersionSummaryPage;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.ReleaseManagementTabPanel;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.TimeZone;

public class ReleasePanelTest
{
    private static final String VERSION_1 = "1.0";
    private static final String VERSION_1_1 = "1.1";
    JiraTestedProduct jira;

    private String projectKey;

    @Before
    public void setup()
    {
        // hack attempting to fix jira's timezone problems
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));

        jira = new JiraTestedProduct(null, JiraBambooProductInstance.INSTANCE);

        projectKey = WebDriverUtils.getNewProjectKey();

        JiraLoginPage page = jira.gotoLoginPage();
        page.loginAsSysAdmin(ViewProjectsPage.class)
                .openCreateProjectDialog()
                .setKey(projectKey)
                .setName("Test " + projectKey)
                .submitSuccess();

        WebDriverVersionUtils.createNewVersion(jira, projectKey, VERSION_1);
    }

    @After
    public void tearDown()
    {
        jira.goTo(ProjectSummaryPageTab.class, projectKey)
                .openOperations()
                .click(DeleteProjectPage.class, ProjectConfigActions.ProjectOperations.DELETE)
                .submitConfirm();
    }

    @Test
    public void testReleasePanelIsVisible() throws Exception
    {
        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey,VERSION_1);
        Assert.assertTrue("Has Release Tab", page.hasTab("bamboo-release-tabpanel-panel"));
    }

    @Test
    public void testCanReleaseWithSuccessfulBuild() throws Exception
    {
        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey,VERSION_1);
        ReleaseManagementTabPanel panel = page.getReleaseManagementTabPanel();
        BambooRelease bambooRelease = panel.executeNewRelease("AP-SUCCEED");
        Assert.assertEquals(BuildState.SUCCESS, bambooRelease.getBuildState());
        WebDriverVersionUtils.assertVersionReleased(jira, projectKey,VERSION_1);
    }

    @Test
    public void testCanNotReleaseIfNewBuildFails() throws Exception
    {
        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey,VERSION_1);
        ReleaseManagementTabPanel panel = page.getReleaseManagementTabPanel();
        BambooRelease bambooRelease = panel.executeNewRelease("AP-FAIL");
        Assert.assertEquals(BuildState.FAILED, bambooRelease.getBuildState());
        WebDriverVersionUtils.assertVersionNotReleased(jira, projectKey,VERSION_1);
    }

    @Test
    public void testCanReleaseWithExistingBuild() throws Exception
    {
        //Run successful build
        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey,VERSION_1);
        ReleaseManagementTabPanel panel = page.getReleaseManagementTabPanel();
        BambooRelease bambooRelease = panel.executeNewRelease("AP-SUCCEED");
        Assert.assertEquals(BuildState.SUCCESS, bambooRelease.getBuildState());

        WebDriverVersionUtils. createNewVersion(jira, projectKey,VERSION_1_1);

        page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey,VERSION_1_1);
        panel = page.getReleaseManagementTabPanel();
        bambooRelease = panel.executeExistingBuildForRelease("AP-SUCCEED");
        Assert.assertEquals(BuildState.SUCCESS, bambooRelease.getBuildState());

        WebDriverVersionUtils.assertVersionReleased(jira, projectKey,VERSION_1);
        WebDriverVersionUtils.assertVersionReleased(jira, projectKey,VERSION_1_1);
    }

    @Test
    public void testVersionNameIsHtmlEncodedInReleasePanel() throws Exception
    {
        final String xssAttempt = "<SCRIPT>alert(\"version\")</SCRIPT>";
        WebDriverVersionUtils. createNewVersion(jira, projectKey, xssAttempt);
        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey, xssAttempt);
        page.getReleaseManagementTabPanel();
        Assert.assertFalse(jira.getTester().getDriver().getPageSource().contains(xssAttempt));
    }

}
