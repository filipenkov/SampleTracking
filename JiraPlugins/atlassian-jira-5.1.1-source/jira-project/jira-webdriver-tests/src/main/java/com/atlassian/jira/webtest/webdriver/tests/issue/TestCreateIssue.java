package com.atlassian.jira.webtest.webdriver.tests.issue;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.components.JiraHeader;
import com.atlassian.jira.pageobjects.components.menu.IssueActions;
import com.atlassian.jira.pageobjects.components.menu.IssuesMenu;
import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.jira.pageobjects.elements.AuiMessage;
import com.atlassian.jira.pageobjects.elements.GlobalMessage;
import com.atlassian.jira.pageobjects.pages.DashboardPage;
import com.atlassian.jira.pageobjects.pages.viewissue.MoreActionsMenu;
import com.atlassian.jira.pageobjects.pages.viewissue.ViewIssuePage;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.webtest.webdriver.tests.common.BaseJiraWebTest;
import com.atlassian.jira.webtests.util.LocalTestEnvironmentData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.FieldMetaData;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueCreateMeta;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.webdriver.AtlassianWebDriver;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests for creating an issue using quick create
 *
 * @since v5.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ISSUES })
@Restore ("xml/TestQuickCreateIssue.xml")
public class TestCreateIssue extends BaseJiraWebTest
{
    @Inject
    private PageBinder pageBinder;

    @Inject
    private PageElementFinder finder;

    @Inject
    private AtlassianWebDriver webDriver;

    IssueClient issueClient;

    @Before
    public void setUp() throws Exception
    {
        issueClient = new IssueClient(new LocalTestEnvironmentData());
    }

    @Test
    public void testTogglingModesRetainsValues()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        viewIssuePage.execKeyboardShortcut("c");

        CreateIssueDialog createIssueDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE)
                .switchToFullMode()
                .fill("summary", "test");

        createIssueDialog = createIssueDialog.switchToCustomMode();
        assertEquals("test", createIssueDialog.getFieldValue("summary"));
        createIssueDialog.fill("description", "test another switch");
        createIssueDialog = createIssueDialog.switchToFullMode();
        assertEquals("test another switch", createIssueDialog.getFieldValue("description"));
        assertEquals("test", createIssueDialog.getFieldValue("summary"));
    }

    @Test
    public void testCustomFieldJavaScriptExecutes()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        assertTrue(finder.find(By.id("customfield_10000")).hasClass("custom-field-js-applied"));
    }

    @Test
    public void testCustomFieldIsDisplayedAndJavaScriptExecutesWhenChecked()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode().addFields("customfield_10000");
        assertTrue(finder.find(By.id("customfield_10000")).hasClass("custom-field-js-applied"));
    }

    @Test
    public void testCreatingSimpleIssueInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode();
        createIssueDialog.fill("summary", "Scott's Issue")
                .fill("description", "I own this issue!");

        GlobalMessage successMsg = createIssueDialog.submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        assertTrue("Expected success message to contain issue summary", successMsg.getMessage().contains("Scott's Issue"));
        assertTrue("Expected success message to contain close button", successMsg.isCloseable());
    }

    @Test
    public void testCreatingSimpleIssueInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        final CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        createIssueDialog.fill("summary", "Scott's Issue")
                .fill("description", "I own this issue!");

        GlobalMessage successMsg = createIssueDialog.submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        assertTrue("Expected success message to contain issue summary", successMsg.getMessage().contains("Scott's Issue"));
    }

    @Test
    public void testValidationErrorsInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode();
        final Map<String, String> formErrors = createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE).waitForFormErrors().getFormErrors();
        assertTrue("Expected inline errror for summary as it is a required field and has no value", formErrors.containsKey("summary"));
    }

    @Test
    public void testValidationErrorsInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        final Map<String, String> formErrors = createIssueDialog.submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE).waitForFormErrors().getFormErrors();
        assertTrue("Expected inline errror for summary as it is a required field and has no value", formErrors.containsKey("summary"));
    }

    @Test
    @Restore ("xml/TestProjectSelectForCreate.xml")
    public void testProjectSelectInCustomMode()
    {
        List<String> issueTypesString;

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode();

        // homosapien project
        issueTypesString = getIssueTypes("HSP");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // gorilla project
        createIssueDialog.selectProject("gorilla");
        issueTypesString = getIssueTypes("GRL");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // monkey project
        createIssueDialog.selectProject("monkey");
        issueTypesString = getIssueTypes("MKY");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));
    }

    @Test
    @Restore ("xml/TestProjectSelectForCreate.xml")
    public void testProjectSelectInFullMode()
    {
        List<String> issueTypesString;

        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();

        // homosapien project
        issueTypesString = getIssueTypes("HSP");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // gorilla project
        createIssueDialog.selectProject("gorilla");
        issueTypesString = getIssueTypes("GRL");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));

        // monkey project
        createIssueDialog.selectProject("monkey");
        issueTypesString = getIssueTypes("MKY");
        Assert.assertEquals(issueTypesString.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypesString), new HashSet<String>(createIssueDialog.getIssueTypes()));
    }

    @Test
    public void testAddingAndRemovingFields()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToCustomMode()
                .removeFields("priority", "description")
                .addFields("fixVersions", "reporter");
        createIssueDialog.close();
        jira.gotoHomePage();
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        assertEquals(createIssueDialog.getVisibleFields(), asList("summary", "components", "versions", "fixVersions", "reporter"));
    }

    @Test
    public void testCreateMultipleInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue()
                .fill("summary", "Issue 1")
                .fill("description", "my description")
                .checkCreateMultiple()
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        assertEquals(AuiMessage.Type.SUCCESS, createIssueDialog.getAuiMessage().getType());

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("my description", createIssueDialog.getFieldValue("description"));

        createIssueDialog = createIssueDialog.fill("summary", "Issue 2")
                .fill("description", "a different description")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        AuiMessage auiMessage = createIssueDialog.getAuiMessage();
        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("a different description", createIssueDialog.getFieldValue("description"));

        GlobalMessage successMsg = createIssueDialog.uncheckCreateMultiple()
                .fill("summary", "Issue 3")
                .submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        assertTrue("Expected success message to contain issue summary", successMsg.getMessage().contains("Issue 3"));
    }

    @Test
    public void testCreateMultipleInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        jira.goToViewIssue("HSP-1");
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue()
                .switchToFullMode()
                .fill("summary", "Issue 1")
                .fill("description", "my description")
                .checkCreateMultiple()
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("my description", createIssueDialog.getFieldValue("description"));

        createIssueDialog = createIssueDialog.fill("summary", "Issue 2")
                .fill("description", "a different description")
                .submit(CreateIssueDialog.class, CreateIssueDialog.Type.ISSUE);


        AuiMessage auiMessage = createIssueDialog.getAuiMessage();
        assertEquals(AuiMessage.Type.SUCCESS, auiMessage.getType());
        auiMessage.dismiss();

        assertEquals("", createIssueDialog.getFieldValue("summary"));
        assertEquals("a different description", createIssueDialog.getFieldValue("description"));

        GlobalMessage successMsg = createIssueDialog.uncheckCreateMultiple()
                .fill("summary", "Issue 3")
                .submit(GlobalMessage.class);

        assertTrue("Expected message to be of type success", successMsg.getType() == GlobalMessage.Type.SUCCESS);
        assertTrue("Expected success message to contain issue summary", successMsg.getMessage().contains("Issue 3"));
    }


    @Test
    @Restore ("xml/TestIssueTypesForCreate.xml")
    public void testIssueTypeSelectInCustomMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class)
                .createIssue()
                .switchToCustomMode();

        createIssueDialog.selectIssueType("Bug");
        assertEquals(asList("versions", "summary"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("New Feature");
        assertEquals(asList("summary", "components"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("Task");
        assertEquals(asList("summary"), createIssueDialog.getVisibleFields());
    }


    @Test
    @Restore ("xml/TestIssueTypesForCreate.xml")
    public void testIssueTypeSelectInFullMode()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.switchToFullMode();
        createIssueDialog.selectIssueType("Bug");
        assertEquals(asList("versions", "summary"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("New Feature");
        assertEquals(asList("summary", "components"), createIssueDialog.getVisibleFields());
        createIssueDialog.selectIssueType("Task");
        assertEquals(asList("assignee", "summary"), createIssueDialog.getVisibleFields());
    }

    @Test
    public void testCreateWithEscGivesDirtyFormWarning()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "A sample Summary");
        createIssueDialog.escape();
        
        assertTrue(createIssueDialog.acceptDirtyFormWarning());

        //Hitting Cancel should not provide a dirty form warning though.
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "A sample Summary");
        createIssueDialog.close();

        assertFalse(createIssueDialog.acceptDirtyFormWarning());
    }

    @Test
    @Restore("xml/TestSubtasksInCreateIssue.xml")
    public void testSubtaskIssueType()
    {
        jira.gotoLoginPage().loginAsSysAdmin(DashboardPage.class);

        // homosapien project - check no subtasks in issue types
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.selectProject("homosapien");

        List<String> issueTypes = getIssueTypes("HSP");
        Assert.assertEquals(issueTypes.size(), createIssueDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueTypes), new HashSet<String>(createIssueDialog.getIssueTypes()));

        createIssueDialog.close();

        // homosapien project - check only subtasks in issue types
        ViewIssuePage viewIssuePage = jira.goToViewIssue("HSP-1");
        final MoreActionsMenu moreActionsMenu = viewIssuePage.getMoreActionsMenu();
        moreActionsMenu.open().clickItem(IssueActions.CREATE_SUBTASK);
        final CreateIssueDialog createSubtaskDialog = pageBinder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);

        List<String> issueSubtasks = getSubtaskTypes("HSP");
        Assert.assertEquals(issueSubtasks.size(), createSubtaskDialog.getIssueTypes().size());
        assertEquals(new HashSet<String>(issueSubtasks), new HashSet<String>(createSubtaskDialog.getIssueTypes()));
    }

    @Test
    public void testNoPermission()
    {
        backdoor.permissionSchemes().removeGroupPermission(0, 11, "jira-users"); // Remove create permissions
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);
        assertFalse(pageBinder.bind(JiraHeader.class).hasCreateLink());
    }

    @Test
    public void testProjectPreselect()
    {
        // Should select first project in list as we have no history
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        assertEquals("homosapien", createIssueDialog.getProject());

        // We create an issue in monkey so the next time it should be a preselected on monkey
        createIssueDialog.selectProject("monkey");
        createIssueDialog.fill("summary", "test").submit(GlobalMessage.class);
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        assertEquals("monkey", createIssueDialog.getProject());


        jira.goToViewIssue("HSP-1");
        createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        assertEquals("homosapien", createIssueDialog.getProject());
    }

    @Test
    public void testIssueTypePreselect()
    {
        // Should select first issue type in list as we have no history
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);
        backdoor.project().setDefaultIssueType(10000, null);
        JiraHeader header = pageBinder.bind(JiraHeader.class);

        CreateIssueDialog createIssueDialog = header.createIssue();
        assertEquals("Bug", createIssueDialog.getIssueType());
        createIssueDialog.close();
    }

     @Test
    public void testDefaultIssueTypePreselect()
    {
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);
        backdoor.project().setDefaultIssueType(10000, "2");
        JiraHeader header = pageBinder.bind(JiraHeader.class);
        CreateIssueDialog createIssueDialog = header.createIssue();
        assertEquals("New Feature", createIssueDialog.getIssueType());
    }

    @Test
    public void testCreatedIssuesGetAddedToHistory()
    {
        jira.gotoLoginPage().login("fred", "fred", DashboardPage.class);
        CreateIssueDialog createIssueDialog = pageBinder.bind(JiraHeader.class).createIssue();
        createIssueDialog.fill("summary", "My Summary").submit(DashboardPage.class);
        JiraHeader header = pageBinder.bind(JiraHeader.class);

        IssuesMenu issuesMenu = header.getIssuesMenu().open();
        List<String> recentIssues = issuesMenu.getRecentIssues();

        assertTrue(recentIssues.get(0).endsWith("My Summary"));
        issuesMenu.createIssue().fill("summary", "My Summary 2").submit(DashboardPage.class);

        header = pageBinder.bind(JiraHeader.class);
        issuesMenu = header.getIssuesMenu().open();
        recentIssues = issuesMenu.getRecentIssues();

        assertTrue(recentIssues.get(0).endsWith("My Summary 2"));
        assertTrue(recentIssues.get(1).endsWith("My Summary"));

    }

    private List<String> getIssueTypes(String projectKey)
    {
        List<String> issueTypesString = new ArrayList<String>();

        IssueCreateMeta meta = issueClient.getCreateMeta(null, asList(new StringList(projectKey)), null, null, IssueCreateMeta.Expand.fields);
        List<IssueCreateMeta.IssueType> issueTypes = meta.projects.get(0).issuetypes;

        for (IssueCreateMeta.IssueType issueType : issueTypes) {

            FieldMetaData fieldMeta = issueType.fields.get("parent");

            // skip sub-tasks
            if (fieldMeta == null || !fieldMeta.required) {
                issueTypesString.add(issueType.name);
            }
        }

        return issueTypesString;
    }

    private List<String> getSubtaskTypes(String projectKey)
    {
        List<String> subtaskTypesString = new ArrayList<String>();

        IssueCreateMeta meta = issueClient.getCreateMeta(null, asList(new StringList(projectKey)), null, null, IssueCreateMeta.Expand.fields);
        List<IssueCreateMeta.IssueType> subtaskTypes = meta.projects.get(0).issuetypes;

        for (IssueCreateMeta.IssueType subtaskType : subtaskTypes) {

            FieldMetaData fieldMeta = subtaskType.fields.get("parent");

            // only sub-tasks
            if (fieldMeta != null && fieldMeta.required) {
                subtaskTypesString.add(subtaskType.name);
            }
        }

        return subtaskTypesString;
    }


}

