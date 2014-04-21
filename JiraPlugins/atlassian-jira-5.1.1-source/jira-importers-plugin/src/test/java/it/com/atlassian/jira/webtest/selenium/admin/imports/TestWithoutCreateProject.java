package it.com.atlassian.jira.webtest.selenium.admin.imports;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.plugins.importer.backdoor.CreateProjectHandlerBackdoorControl;
import com.atlassian.jira.plugins.importer.po.common.ImporterProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvProjectMappingsPage;
import com.atlassian.jira.plugins.importer.po.csv.CsvSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.fogbugz.hosted.FogBugzHostedProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.pivotal.PivotalImporterSetupPage;
import com.atlassian.jira.plugins.importer.po.pivotal.PivotalProjectsMappingsPage;
import com.atlassian.jira.plugins.importer.po.trac.TracSetupPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

public class TestWithoutCreateProject extends BaseJiraWebTest {

    private CreateProjectHandlerBackdoorControl createProjectHandlerBackdoorControl;

    @Before
    public void setUp() {
        createProjectHandlerBackdoorControl = new CreateProjectHandlerBackdoorControl(jira.environmentData());
        createProjectHandlerBackdoorControl.cantCreateProjects();
    }

    @After
    public void enabledCreateProject() {
        createProjectHandlerBackdoorControl.canCreateProjects();
    }

    @Test
    public void createIsNotAvailableInCsv() {
        CsvSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(CsvSetupPage.class);

        setupPage.setCsvFile(ITUtils.getCsvResource("comments.csv"));
        CsvProjectMappingsPage projectMappingsPage = setupPage.next();
        assertFalse(projectMappingsPage.isCreateProjectAvailable());
    }

    @Test
    public void createIsNotAvailableInFogBugz() {
        final String url = ITUtils.getProperties().getString("fogbugz.ondemand.siteUrl"),
            username = ITUtils.getProperties().getString("fogbugz.ondemand.siteUsername"),
            password = ITUtils.getProperties().getString("fogbugz.ondemand.sitePassword");

        FogBugzHostedProjectsMappingsPage projectsMappingsPage = jira.gotoLoginPage().loginAsSysAdmin(FogBugzHostedImporterSetupPage.class)
                .setSiteUrl(url)
                .setSiteUsername(username)
                .setSitePassword(password).next();

        assertFalse(projectsMappingsPage.isCreateProjectAvailable("Custom workflow"));
    }

    @Test
    public void createIsNotAvailableInPivotal() {
        final String username = ITUtils.getProperties().getString("pivotal.username"),
            password = ITUtils.getProperties().getString("pivotal.password");

        PivotalProjectsMappingsPage projectsMappingsPage = jira.gotoLoginPage()
                .loginAsSysAdmin(PivotalImporterSetupPage.class)
                .setUsername(username)
                .setPassword(password).next();

        assertFalse(projectsMappingsPage.isCreateProjectAvailable("My Sample Project"));
    }

    @Test
    public void createIsNotAvailableForTrac() {
        backdoor.applicationProperties().setOption(APKeys.JIRA_OPTION_ALLOWATTACHMENTS, true);
        ImporterProjectsMappingsPage projectsMappingsPage = jira.visit(LoginPage.class)
                .loginAsSysAdmin(TracSetupPage.class).setEnvironmentFile(ITUtils.getCurrentWorkingDirectory() + "/src/test/resources/trac/trac-sqlite.zip").next();

        assertFalse(projectsMappingsPage.isCreateProjectAvailable("My example project"));
    }

}