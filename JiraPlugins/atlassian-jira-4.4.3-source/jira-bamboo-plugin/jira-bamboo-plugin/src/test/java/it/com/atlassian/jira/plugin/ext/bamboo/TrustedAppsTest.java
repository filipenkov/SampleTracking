//package it.com.atlassian.jira.plugin.ext.bamboo;
//
//import com.atlassian.bamboo.webdriver.BambooTestedProduct;
//import com.atlassian.jira.pageobjects.JiraTestedProduct;
//import com.atlassian.jira.pageobjects.pages.JiraLoginPage;
//import com.atlassian.jira.pageobjects.project.DeleteProjectPage;
//import com.atlassian.jira.pageobjects.project.ProjectConfigActions;
//import com.atlassian.jira.pageobjects.project.ViewProjectsPage;
//import com.atlassian.jira.pageobjects.project.summary.ProjectSummaryPageTab;
//import com.atlassian.jira.pageobjects.project.versions.VersionPageTab;
//import com.atlassian.jira.plugin.ext.bamboo.model.BuildState;
//import com.atlassian.webdriver.AtlassianWebDriver;
//import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.ApplicationLinksAdminPage;
//import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BambooRelease;
//import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.BrowseVersionSummaryPage;
//import it.com.atlassian.jira.plugin.ext.bamboo.pageobjects.ReleaseManagementTabPanel;
//import org.junit.After;
//import org.junit.Assert;
//import org.junit.Before;
//import org.junit.Test;
//import org.openqa.selenium.By;
//import org.openqa.selenium.WebElement;
//
//import java.util.List;
//
//public class TrustedAppsTest
//{
//    protected static final String VERSION_1 = "1.0";
//
//    private JiraTestedProduct jira;
//    private BambooTestedProduct bamboo;
//    private String projectKey;
//
//    @Before
//    public void setup()
//    {
//        bamboo = new BambooTestedProduct(null, BambooProductInstance.INSTANCE);
//        jira = new JiraTestedProduct(null, JiraBambooProductInstance.INSTANCE);
//        projectKey = WebDriverUtils.getNewProjectKey();
//
//        JiraLoginPage page = jira.gotoLoginPage();
//        ViewProjectsPage viewProjectsPage = page.loginAsSysAdmin(ViewProjectsPage.class);
//
//        viewProjectsPage
//                .openCreateProjectDialog()
//                .setKey(projectKey)
//                .setName("Test " + projectKey)
//                .submitSuccess();
//
//        WebDriverVersionUtils.createNewVersion(jira, projectKey, VERSION_1);
//
//        VersionPageTab versionPageTab = jira.goTo(VersionPageTab.class, projectKey);
//        versionPageTab.getEditVersionForm().fill(VERSION_1, "A New Test Version", "9/Jan/20").submit();
//    }
//
//    @After
//    public void tearDown()
//    {
//        jira.goTo(ProjectSummaryPageTab.class, projectKey)
//                .openOperations()
//                .click(DeleteProjectPage.class, ProjectConfigActions.ProjectOperations.DELETE)
//                .submitConfirm();
//    }
//
//    @Test
//    public void testTrustedWithSameUserBaseCreatesTrustedApplications() throws Exception
//    {
//        AtlassianWebDriver driver = jira.getTester().getDriver();
//
//        String url = bamboo.getProductInstance().getBaseUrl();
//
//        ApplicationLinksAdminPage applicationLinksAdminPage = jira.goTo(ApplicationLinksAdminPage.class);
//        applicationLinksAdminPage.clearAllApplicationLinks();
//
//        applicationLinksAdminPage.addApplicationLink()
//                .withServerUrl(url)
//                .withReciprocalLink(null, "admin", "admin")
//                .withSameUsers()
//                .withTrustedRelationship()
//                .submit();
//
//
//        driver.waitUntilElementIsVisible(By.id("application-links-table"));
//
//
//        WebElement appLink = driver.findElement(By.cssSelector("#application-links-table tr .application-url"));
//        Assert.assertTrue(appLink.getText().contains(url));
//
//        List<WebElement> incomingAuths = driver.findElements(By.cssSelector("#application-links-table tr .application-incoming-authentication"));
//        Assert.assertTrue(incomingAuths.size() == 1);
//        Assert.assertTrue(incomingAuths.get(0).getText().contains("Trusted Applications"));
//
//        List<WebElement> outgoingAuths = driver.findElements(By.cssSelector("#application-links-table tr .application-outgoing-authentication"));
//        Assert.assertTrue(outgoingAuths.size() == 1);
//        Assert.assertTrue(outgoingAuths.get(0).getText().contains("Trusted Applications"));
//
//        testRelease();
//    }
//
//    // generic test release, assumes authentication already set up.
//    private void testRelease() throws Exception
//    {
//        BrowseVersionSummaryPage page = WebDriverVersionUtils.getBrowseVersionSummaryPageForTestProject(jira, projectKey, VERSION_1);
//        ReleaseManagementTabPanel panel = page.getReleaseManagementTabPanel();
//        BambooRelease bambooRelease = panel.executeNewRelease("AP-SUCCEED");
//        Assert.assertEquals(BuildState.SUCCESS, bambooRelease.getBuildState());
//        WebDriverVersionUtils.assertVersionReleased(jira, projectKey, VERSION_1);
//    }
//
//
//}
