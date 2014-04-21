package it.com.atlassian.jira.webtest.selenium.admin.imports.json;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugins.importer.po.common.ImporterFinishedPage;
import com.atlassian.jira.plugins.importer.po.json.JsonSetupPage;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.pageobjects.page.LoginPage;
import it.com.atlassian.jira.webtest.selenium.admin.imports.ITUtils;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebTest({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
public class TestJsonImport extends BaseJiraWebTest {

    @Before
    public void setUpTest() {
        backdoor.restoreData("blankprojects.xml");
    }

    @Test
    public void importBugzilla() {
        JsonSetupPage setupPage = jira.visit(LoginPage.class).loginAsSysAdmin(JsonSetupPage.class);

        ImporterFinishedPage finishPage = setupPage.setJsonFile(ITUtils.getResource("/json/bugzilla.json")).next().waitUntilFinished();
        assertTrue(finishPage.isSuccess());
        assertEquals("2", finishPage.getProjectsImported());
        assertEquals("45", finishPage.getIssuesImported());
    }

}
