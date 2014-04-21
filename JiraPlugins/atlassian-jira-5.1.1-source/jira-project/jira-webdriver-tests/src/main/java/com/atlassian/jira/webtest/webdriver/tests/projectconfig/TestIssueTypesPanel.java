package com.atlassian.jira.webtest.webdriver.tests.projectconfig;

import com.atlassian.integrationtesting.runner.restore.RestoreOnce;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.project.issuetypes.IssueType;
import com.atlassian.jira.pageobjects.project.issuetypes.IssueTypesPage;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueCreateMeta;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for the notifications panel.
 *
 * @since v4.4
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION, Category.PLUGINS, Category.PROJECTS })
@RestoreOnce ("xml/TestProjectConfigIssueTypesTab.xml")
public class TestIssueTypesPanel extends BaseJiraWebTest
{
    private static final String PROJECT_WITH_CONFIGURED_SCHEMES = "MKY";
    private static final String PROJECT_WITH_DEFAULT_ISSUE_TYPES = "HSP";
    private static final String DEFAULT_WORKFLOW_NAME = "jira";
    private static final String BASE_WORKFLOW_URL = "/secure/admin/workflows/ViewWorkflowSteps.jspa?workflowMode=live&workflowName=";
    private static final String DEFAULT_FIELD_CONFIGURATION_NAME = "System Default Field Configuration";
    private static final String BASE_FIELD_CONFIGURATION_URL = "/secure/admin/ConfigureFieldLayout!default.jspa";
    private static final String DEFAULT_SCREEN_SCHEME_NAME = "Default Screen Scheme";
    private static final String BASE_SCREEN_SCHEME_URL = "/secure/admin/ConfigureFieldScreenScheme.jspa?id=";

    IssueClient issueClient;

    @Before
    public void setUp() throws Exception
    {
        issueClient = new IssueClient(new LocalTestEnvironmentData());
    }

    @Test
    public void testDefault()
    {
        IssueTypesPage issueTypesPage = jira.gotoLoginPage().loginAsSysAdmin(IssueTypesPage.class, PROJECT_WITH_DEFAULT_ISSUE_TYPES);

        // Assert the edit and change links in the header
        assertTrue(issueTypesPage.isSchemeLinked());
        assertTrue(issueTypesPage.isSchemeChangeAvailable());
        assertEquals("Default Issue Type Scheme", issueTypesPage.getSchemeName());

        List<IssueType> issueTypes = issueTypesPage.getIssueTypes();

        int issueTypesNum = getAllIssueTypes(PROJECT_WITH_DEFAULT_ISSUE_TYPES).size();

        // There should be just the 4 default issue types, linking to the default schemes
        assertEquals(issueTypesNum, issueTypes.size());
        assertWithDefaults(issueTypes, "Bug", true, false);
        assertWithDefaults(issueTypes, "New Feature", false, false);
        assertWithDefaults(issueTypes, "Task", false, false);
        assertWithDefaults(issueTypes, "Improvement", false, false);
        assertWithDefaults(issueTypes, "Regression", false, false);
        assertWithDefaults(issueTypes, "QA", false, true);
        assertWithDefaults(issueTypes, "Doco", false, true);
    }

    @Test
    public void testConfigured()
    {
        IssueTypesPage issueTypesPage = jira.gotoLoginPage().loginAsSysAdmin(IssueTypesPage.class, PROJECT_WITH_CONFIGURED_SCHEMES);

        // Assert the edit and change links in the header
        assertTrue(issueTypesPage.isSchemeLinked());
        assertTrue(issueTypesPage.isSchemeChangeAvailable());
        assertEquals("Test Issue Type Scheme", issueTypesPage.getSchemeName());

        List<IssueType> issueTypes = issueTypesPage.getIssueTypes();

        assertEquals(5, issueTypes.size());
        assertIssueType(issueTypes, "Regression", false, false, getWorkflowLink("TestFlow2"), null,
                getFieldConfigurationLink("TestFieldConfig1", "10000"), null,
                getScreenSchemeLink("Regressing Screen Scheme", "10000"), null);
        assertIssueType(issueTypes, "Bug", true, false, getWorkflowLink(DEFAULT_WORKFLOW_NAME), null,
                getFieldConfigurationLink(DEFAULT_FIELD_CONFIGURATION_NAME, null), null,
                getScreenSchemeLink(DEFAULT_SCREEN_SCHEME_NAME, "1"), null);
        assertIssueType(issueTypes, "New Feature", false, false, getWorkflowLink("TestFlow1"), null,
                getFieldConfigurationLink(DEFAULT_FIELD_CONFIGURATION_NAME, null), null,
                getScreenSchemeLink("Regressing Screen Scheme", "10000"), null);
        assertWithDefaults(issueTypes, "QA", false, true);
        assertWithDefaults(issueTypes, "Doco", false, true);

    }

    @Test
    public void testNotAdminIssueTypes()
    {
        IssueTypesPage issueTypesPage = jira.gotoLoginPage().login("fred", "fred", IssueTypesPage.class, PROJECT_WITH_DEFAULT_ISSUE_TYPES);

        // Assert the cog actions aren't present
        assertFalse(issueTypesPage.isSchemeLinked());
        assertFalse(issueTypesPage.isSchemeChangeAvailable());

        List<IssueType> issueTypes = issueTypesPage.getIssueTypes();

        int issueTypesNum = getAllIssueTypes(PROJECT_WITH_DEFAULT_ISSUE_TYPES).size();

        assertEquals(issueTypesNum, issueTypes.size());
        assertWithDefaultsNoAdmin(issueTypes, "Bug", true, false);
        assertWithDefaultsNoAdmin(issueTypes, "New Feature", false, false);
        assertWithDefaultsNoAdmin(issueTypes, "Task", false, false);
        assertWithDefaultsNoAdmin(issueTypes, "Improvement", false, false);
        assertWithDefaultsNoAdmin(issueTypes, "Regression", false, false);
        assertWithDefaultsNoAdmin(issueTypes, "QA", false, true);
        assertWithDefaultsNoAdmin(issueTypes, "Doco", false, true);
    }

    private void assertWithDefaults(List<IssueType> issueTypes, String issueTypeName, boolean defaultIssueType, boolean subTask)
    {
        assertIssueType(issueTypes, issueTypeName, defaultIssueType, subTask,
                getWorkflowLink(DEFAULT_WORKFLOW_NAME), null,
                getFieldConfigurationLink(DEFAULT_FIELD_CONFIGURATION_NAME, null), null,
                getScreenSchemeLink(DEFAULT_SCREEN_SCHEME_NAME, "1"), null);
    }

    private void assertWithDefaultsNoAdmin(List<IssueType> issueTypes, String issueTypeName, boolean defaultIssueType, boolean subTask)
    {
        assertIssueType(issueTypes, issueTypeName, defaultIssueType, subTask,
                null, DEFAULT_WORKFLOW_NAME,
                null, DEFAULT_FIELD_CONFIGURATION_NAME,
                null, DEFAULT_SCREEN_SCHEME_NAME);
    }

    private void assertIssueType(List<IssueType> issueTypes, String name, boolean defaultIssueType, boolean subTask,
            IssueType.Link workflowLink, String workflowName,
            IssueType.Link fieldConfigLink, String fieldConfigName,
            IssueType.Link screenSchemeLink, String screenSchemeName)
    {
        // Find the entry in the list
        for (IssueType issueType : issueTypes)
        {
            if (issueType.getName().equals(name))
            {
                // assert the list is the same as that provided
                assertEquals("Default property did not match for :" + name, defaultIssueType, issueType.isDefaultIssueType());
                assertEquals("SubTask property did not match for :" + name, subTask, issueType.isSubtask());
                assertEquals("Workflow link did not match for :" + name, workflowLink, issueType.getWorkflow());
                assertEquals("Workflow name did not match for :" + name, workflowName, issueType.getWorkflowName());
                assertEquals("Field config link did not match for :" + name, fieldConfigLink, issueType.getFieldLayout());
                assertEquals("Field config name did not match for :" + name, fieldConfigName, issueType.getFieldLayoutName());
                assertEquals("Screen scheme link did not match for :" + name, screenSchemeLink, issueType.getScreenScheme());
                assertEquals("Screen scheme name did not match for :" + name, screenSchemeName, issueType.getScreenSchemeName());
                return;
            }
        }
        fail("Expected issueType '" + name + "' not found.");
    }

    private IssueType.Link getWorkflowLink(String name)
    {
        return new IssueType.Link(name, jira.getProductInstance().getBaseUrl() + BASE_WORKFLOW_URL + name);
    }

    private IssueType.Link getFieldConfigurationLink(String name, String id)
    {
        String url = BASE_FIELD_CONFIGURATION_URL;
        if (id != null)
        {
            url = url + "?id=" + id;
        }
        return new IssueType.Link(name, jira.getProductInstance().getBaseUrl() + url);
    }

    private IssueType.Link getScreenSchemeLink(String name, String id)
    {
        return new IssueType.Link(name, jira.getProductInstance().getBaseUrl() + BASE_SCREEN_SCHEME_URL + id);
    }

    private List<String> getAllIssueTypes(String projectKey)
    {
        List<String> issueTypesString = new ArrayList<String>();

        IssueCreateMeta meta = issueClient.getCreateMeta(null, asList(new StringList(projectKey)), null, null, IssueCreateMeta.Expand.fields);
        List<IssueCreateMeta.IssueType> issueTypes = meta.projects.get(0).issuetypes;

        for (IssueCreateMeta.IssueType issueType : issueTypes)
        {
            issueTypesString.add(issueType.name);
        }

        return issueTypesString;
    }


}
