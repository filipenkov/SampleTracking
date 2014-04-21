package it.com.atlassian.jira.plugin.ext.bamboo;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
import com.atlassian.jira.pageobjects.pages.project.browseversion.BrowseVersionPage;
import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.timeout.TimeoutType;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.ReleaseDialog;
import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.ReleaseManagementTab;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class ReleasePanelTest
{
    private static final String VERSION_1 = "1.0";
    private static final String VERSION_1_1 = "1.1";

    private JiraTestedProduct jira;

    private String projectKey;

    @Before
    public void setup()
    {
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
        BrowseVersionPage browseVersions = WebDriverVersionUtils.goToBrowseVersionPageFor(jira, projectKey, VERSION_1);
        assertTrue("Browse versions page should have release tab", browseVersions.hasTab(ReleaseManagementTab.class));
    }

    @Test
    public void testCanReleaseWithSuccessfulBuild() throws Exception
    {
        ReleaseManagementTab panel = openReleasePanelFor(VERSION_1);
        assertEquals(BuildState.SUCCESS, panel.executeNewRelease("AP-SUCCEED"));
        WebDriverVersionUtils.assertVersionReleased(jira, projectKey, VERSION_1);
    }

    @Test
    public void testCanNotReleaseIfNewBuildFails() throws Exception
    {
        ReleaseManagementTab panel = openReleasePanelFor(VERSION_1);
        assertEquals(BuildState.FAILED, panel.executeNewRelease("AP-FAIL"));
        WebDriverVersionUtils.assertVersionNotReleased(jira, projectKey,VERSION_1);
    }

    @Test
    public void testVersionNameIsHtmlEncodedInReleasePanel() throws Exception
    {
        final String xssAttempt = "<SCRIPT>alert(\"version\")</SCRIPT>";
        WebDriverVersionUtils.createNewVersion(jira, projectKey, xssAttempt);
        BrowseVersionPage page = WebDriverVersionUtils.goToBrowseVersionPageFor(jira, projectKey, xssAttempt);
        page.openTab(ReleaseManagementTab.class);
        Assert.assertFalse(jira.getTester().getDriver().getPageSource().contains(xssAttempt));
    }
    
    @Test
    public void testCanReleaseWithExistingBuild() throws Exception
    {
        WebDriverVersionUtils.createNewVersion(jira, projectKey, VERSION_1_1);

        final BuildState stateForVersion1 = openReleasePanelFor(VERSION_1).executeNewRelease("AP-SUCCEED");
        assertEquals(BuildState.SUCCESS, stateForVersion1);

        final BuildState stateForVersion11 = runFirstAvailableExistingBuildForRelease(VERSION_1_1, "AP-SUCCEED");
        assertEquals(BuildState.SUCCESS, stateForVersion11);

        WebDriverVersionUtils.assertVersionReleased(jira, projectKey,VERSION_1);
        WebDriverVersionUtils.assertVersionReleased(jira, projectKey,VERSION_1_1);
    }

    private BuildState runFirstAvailableExistingBuildForRelease(String version, String planKey, String... stages)
    {
        final ReleaseManagementTab releaseManagementTab = openReleasePanelFor(version);
        final ReleaseDialog dialog = releaseManagementTab.openReleaseDialog().selectExistingBuild(planKey);
        final List<PageElement> builds = dialog.allExistingBuilds();
        assertFalse("Must have at least one existing build", builds.isEmpty());
        builds.get(0).select();
        dialog.unselectAllStages().selectStages(stages).submit();
        return releaseManagementTab.waitForBuildToFinish(TimeoutType.PAGE_LOAD).getBuildStatus();
    }

    private ReleaseManagementTab openReleasePanelFor(String versionName)
    {
        return WebDriverVersionUtils.goToBrowseVersionPageFor(jira, projectKey, versionName).openTab(ReleaseManagementTab.class);
    }

}
