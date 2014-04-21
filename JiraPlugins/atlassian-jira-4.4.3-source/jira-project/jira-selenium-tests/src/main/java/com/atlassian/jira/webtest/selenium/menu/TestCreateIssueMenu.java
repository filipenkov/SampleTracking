package com.atlassian.jira.webtest.selenium.menu;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.jira.webtest.selenium.framework.Window;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.00
 */
@SkipInBrowser(browsers={Browser.IE}) //JS Error - Responsibility: JIRA TEam
@WebTest({Category.SELENIUM_TEST })
public class TestCreateIssueMenu extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String TEST_USERNAME = "test";
    private static final String TEST_PASSWORD = "test";

    private static final String RECENT_PROJECTS = "//select[@id='quick-pid']/optgroup[@label='Recent Projects']/option[<no>][@value='<id>']";
    private static final String ALL_PROJECTS = "//select[@id='quick-pid']/optgroup[@label='All Projects']/option[<no>][@value='<id>']";
    private static final String NO_OPTGROUP_PROJECTS = "//select[@id='quick-pid']/option[<no>][@value='<id>']";

    private static final String ISSUE_TYPE_XPATH = "//select[@id='quick-issuetype']/option[<no>][@value='<id>']";
    private static final String NO_ISSUE_TYPE_XPATH = "//select[@id='quick-issuetype']/option[@value='<id>']";

    private static final String MANAGE_ISSUE_TYPE_LINK = "//a[@href='ManageIssueTypeSchemes!default.jspa']";
    private static final String CREATE_ISSUE_LINK = "createItem";
    private static final String CREATE_BUTTON = "//div[@class='buttons']//input[@id='quick-create-button']";

    private static final int MAX_HISTORY_SIZE = 18;
    private static final int FLUSHED_HISTORY_SIZE = 9;

    private static final Map<String, String> PROJECT_NAME_TO_ID = new LinkedHashMap<String, String>();
    private static final Map<String, String> ISSUE_NAME_TO_ID = new LinkedHashMap<String, String>();
    private static final List<String> ISSUETYPE_LIST = new ArrayList<String>();
    private static final List<String> PROJECT_LIST = new ArrayList<String>();
    private static final List<String> PROJECT_LIST_NO_S = new ArrayList<String>();
    private static final List<String> recentProjects = new ArrayList<String>();

    public static Test suite()
    {
        return suiteFor(TestCreateIssueMenu.class);
    }

    public void onSetUp()
    {
        super.onSetUp();

        PROJECT_NAME_TO_ID.put("TPA", "10004");
        PROJECT_NAME_TO_ID.put("TPB", "10003");
        PROJECT_NAME_TO_ID.put("TPC", "10005");
        PROJECT_NAME_TO_ID.put("TPD", "10006");
        PROJECT_NAME_TO_ID.put("TPE", "10007");
        PROJECT_NAME_TO_ID.put("TPF", "10008");
        PROJECT_NAME_TO_ID.put("TPG", "10009");
        PROJECT_NAME_TO_ID.put("TPH", "10010");
        PROJECT_NAME_TO_ID.put("TPI", "10011");
        PROJECT_NAME_TO_ID.put("TPJ", "10012");
        PROJECT_NAME_TO_ID.put("TPK", "10013");
        PROJECT_NAME_TO_ID.put("TPL", "10020");
        PROJECT_NAME_TO_ID.put("TPM", "10021");
        PROJECT_NAME_TO_ID.put("TPN", "10022");
        PROJECT_NAME_TO_ID.put("TPO", "10023");
        PROJECT_NAME_TO_ID.put("TPP", "10024");
        PROJECT_NAME_TO_ID.put("TPQ", "10025");
        PROJECT_NAME_TO_ID.put("TPR", "10030");
        PROJECT_NAME_TO_ID.put("TPS", "10040");

        ISSUE_NAME_TO_ID.put("Bug", "1");
        ISSUE_NAME_TO_ID.put("New Feature", "2");
        ISSUE_NAME_TO_ID.put("Task", "3");
        ISSUE_NAME_TO_ID.put("Improvement", "4");
        ISSUE_NAME_TO_ID.put("Issue", "5");
        ISSUE_NAME_TO_ID.put("Sub-task", "6");

        PROJECT_LIST.addAll(PROJECT_NAME_TO_ID.keySet());
        ISSUETYPE_LIST.addAll(ISSUE_NAME_TO_ID.keySet());
        PROJECT_LIST_NO_S.addAll(PROJECT_NAME_TO_ID.keySet());
        PROJECT_LIST_NO_S.remove("TPS");

    }

    public void checkQuickIssuetype(String... issueNames)
    {
        List<String> issuesNotPresent = new ArrayList<String>(ISSUETYPE_LIST);
        for (int i = 0; i < issueNames.length; ++i)
        {
            assertThat.elementContainsText(ISSUE_TYPE_XPATH.replace("<id>", ISSUE_NAME_TO_ID.get(issueNames[i])).replace("<no>", "" + (i + 1)), issueNames[i]);
            issuesNotPresent.remove(issueNames[i]);
        }

        for (String issueNotPresent : issuesNotPresent)
        {
            assertThat.elementNotPresent(NO_ISSUE_TYPE_XPATH.replace("<id>", ISSUE_NAME_TO_ID.get(issueNotPresent)));
        }
    }

    public void checkQuickPID(List<String> projects, String xpath)
    {
        for (int i = 0; i < projects.size(); ++i)
        {
            assertThat.elementContainsText(xpath.replace("<id>", PROJECT_NAME_TO_ID.get(projects.get(i))).replace("<no>", "" + (i + 1)), projects.get(i));
        }
    }

    public void addHistory(String project)
    {
        recentProjects.remove(project);
        recentProjects.add(0, project);
        if (recentProjects.size() > MAX_HISTORY_SIZE)
        {
            List<String> newRecentProjects = new ArrayList<String>(recentProjects.subList(0, FLUSHED_HISTORY_SIZE));
            recentProjects.retainAll(newRecentProjects);
        }
    }

    public void testCreateIssueMenu()
    {
        restoreData("Blank.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        _testNoProject();
        restoreData("TestCreateIssueMenu.xml");
        _testNoCreatePermissions();
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
            //_testCtrlC();
        _testProjectNoHistory();
        _testProjectWithHistory();
        _testHistoryChangeOrder();
        _testHistoryHasAllProjects();
        _testHistoryOverload();


        _testProjectsWithDifferentIssueSchemes();
        _testIssueTypeDeleted();
        _testIssueTypeChangeOrder();
        _testIssueTypeAdd();
        _testIssueSubTaskNotPresent();
        _testIssueSchemesChanged();

        _testIssueTypeDefault();


        _testProjectPermissionsChanged();
        _testProjectDeleted();
        //Test no run due to JRA-18112
        //_testDeleteProjectWhileDropdownOpen();
        _testDeleteIssueTypeWhileDropdownOpen();


    }

    private void _testIssueSubTaskNotPresent()
    {
        getNavigator().gotoAdmin();
        client.click("subtasks", true);
        client.click("enable_subtasks", true);

        openCreateIssueDialog();
        
        client.select("quick-pid", "label=TPG");
        checkQuickIssuetype("Bug",
                "New Feature",
                "Task",
                "Improvement",
                "Issue"
        );
    }

    //This currently does not work in selenium for unknown reasons
    /*private void _testCtrlC()
    {
        assertThat.elementNotPresentByTimeout(CREATE_BUTTON, TIMEOUT);
        client.controlKeyDown();
        client.keyPress("jira", "c");
        client.controlKeyUp();
        assertThat.visibleByTimeout(CREATE_BUTTON, TIMEOUT);        
    }*/

    private void _testNoProject()
    {
        assertThat.elementNotPresent(CREATE_ISSUE_LINK);

    }

    private void _testNoCreatePermissions()
    {
        getNavigator().login(TEST_USERNAME, TEST_PASSWORD);
        assertThat.elementNotPresent(CREATE_ISSUE_LINK);
        getNavigator().logout(getXsrfToken());
    }


    private void _testProjectNoHistory()
    {
        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST_NO_S, NO_OPTGROUP_PROJECTS);
    }

    private void _testProjectWithHistory()
    {
        getNavigator().browseProject("TPA");
        openCreateIssueDialog();

        checkQuickPID(PROJECT_LIST_NO_S, ALL_PROJECTS);

        addHistory("TPA");
        checkQuickPID(recentProjects, RECENT_PROJECTS);

    }

    private void _testHistoryChangeOrder()
    {
        getNavigator().browseProject("TPB");
        addHistory("TPB");
        getNavigator().browseProject("TPC");
        addHistory("TPC");

        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST_NO_S, ALL_PROJECTS);
        checkQuickPID(recentProjects, RECENT_PROJECTS);

        getNavigator().browseProject("TPB");
        addHistory("TPB");
        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST_NO_S, ALL_PROJECTS);
        checkQuickPID(recentProjects, RECENT_PROJECTS);

    }


    private void _testHistoryHasAllProjects()
    {
        getNavigator().browseProject("TPD");
        addHistory("TPD");
        getNavigator().browseProject("TPE");
        addHistory("TPE");
        getNavigator().browseProject("TPF");
        addHistory("TPF");
        getNavigator().browseProject("TPG");
        addHistory("TPG");
        getNavigator().browseProject("TPH");
        addHistory("TPH");
        getNavigator().browseProject("TPI");
        addHistory("TPI");
        getNavigator().browseProject("TPJ");
        addHistory("TPJ");
        getNavigator().browseProject("TPK");
        addHistory("TPK");
        getNavigator().browseProject("TPL");
        addHistory("TPL");
        getNavigator().browseProject("TPM");
        addHistory("TPM");
        getNavigator().browseProject("TPN");
        addHistory("TPN");
        getNavigator().browseProject("TPO");
        addHistory("TPO");
        getNavigator().browseProject("TPP");
        addHistory("TPP");
        getNavigator().browseProject("TPQ");
        addHistory("TPQ");
        getNavigator().browseProject("TPR");
        addHistory("TPR");
        openCreateIssueDialog();
        checkQuickPID(recentProjects, NO_OPTGROUP_PROJECTS);
    }

    private void _testHistoryOverload()
    {
        getAdministration().createProject("TPS", "TPS", ADMIN_USERNAME);

        getNavigator().browseProject("TPS");
        addHistory("TPS");

        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST, ALL_PROJECTS);
        checkQuickPID(recentProjects, RECENT_PROJECTS);


    }

    private void _testProjectsWithDifferentIssueSchemes()
    {
        openCreateIssueDialog();

        client.select("quick-pid", "label=TPA");
        checkQuickIssuetype("Bug");

        client.select("quick-pid", "label=TPB");
        checkQuickIssuetype("Improvement");

        client.select("quick-pid", "label=TPC");
        checkQuickIssuetype("Improvement",
                "Bug",
                "Issue"
        );

        client.select("quick-pid", "label=TPD");
        checkQuickIssuetype("Bug",
                "New Feature",
                "Task",
                "Improvement",
                "Issue"
        );


    }

    private void _testIssueTypeDeleted()
    {
        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click("//a[@href='DeleteIssueType!default.jspa?id=5']", true);
        client.click("Delete", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPC");
        checkQuickIssuetype("Improvement",
                "Bug"
        );

        client.select("quick-pid", "label=TPD");
        checkQuickIssuetype("Bug",
                "New Feature",
                "Task",
                "Improvement"
        );
    }

    private void _testIssueTypeChangeOrder()
    {
        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click(MANAGE_ISSUE_TYPE_LINK, true);
        client.click("edit_10012", true);

        client.dragAndDropToObject("selectedOptions_4", "selectedOptions");
        client.click("Save", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPC");
        checkQuickIssuetype("Bug",
                "Improvement"
        );
    }

    private void _testIssueTypeAdd()
    {
        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.typeInElementWithName("name", "Issue");
        client.click("Add", true);

        openCreateIssueDialog();

        client.select("quick-pid", "label=TPA");
        checkQuickIssuetype("Bug");

        client.select("quick-pid", "label=TPB");
        checkQuickIssuetype("Improvement");

        client.select("quick-pid", "label=TPC");
        checkQuickIssuetype("Bug",
                "Improvement"
        );

        client.select("quick-pid", "label=TPD");
        checkQuickIssuetype("Bug",
                "New Feature",
                "Task",
                "Improvement",
                "Issue"
        );


    }

    private void _testIssueSchemesChanged()
    {
        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click(MANAGE_ISSUE_TYPE_LINK, true);

        client.click("associate_10011", true);
        client.select("name=projects", "label=TPA");
        client.click("Associate", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPA");
        checkQuickIssuetype("Improvement");

        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click(MANAGE_ISSUE_TYPE_LINK, true);
        client.click("associate_10012", true);
        client.select("name=projects", "label=TPB");
        client.click("Associate", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPB");
        checkQuickIssuetype("Bug",
                "Improvement"
        );

        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click(MANAGE_ISSUE_TYPE_LINK, true);
        client.click("associate_10010", true);
        client.select("name=projects", "label=TPC");
        client.click("Associate", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPC");
        checkQuickIssuetype("Bug");

        client.select("quick-pid", "label=TPD");
        checkQuickIssuetype("Bug",
                "New Feature",
                "Task",
                "Improvement",
                "Issue"
        );

    }

    private void _testIssueTypeDefault()
    {
        openCreateIssueDialog();
        client.select("quick-pid", "label=TPB");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Improvement"));
        client.select("quick-pid", "label=TPD");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Bug"));


        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click(MANAGE_ISSUE_TYPE_LINK, true);
        client.click("edit_10000", true);
        client.select("defaultOption_select", "label=Issue");
        client.click("Save", true);

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPD");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Issue"));
        client.select("quick-pid", "label=TPC");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Bug"));

    }


    private void _testProjectPermissionsChanged()
    {
        getNavigator().gotoPage("/plugins/servlet/project-config/" + "TPS" + "/permissions", true);
        client.click("project-config-permissions-scheme-change", true);
        client.select("schemeIds_select", "label=NoCreateIssue");
        client.click("Associate", true);

        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST_NO_S, ALL_PROJECTS);
        recentProjects.remove("TPS");
        checkQuickPID(recentProjects, RECENT_PROJECTS);

        getNavigator().gotoPage("/plugins/servlet/project-config/" + "TPS" + "/permissions", true);
        client.click("project-config-permissions-scheme-change", true);
        client.select("schemeIds_select", "label=Default Permission Scheme");
        client.click("Associate", true);

        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST, ALL_PROJECTS);
        addHistory("TPS");
        checkQuickPID(recentProjects, RECENT_PROJECTS);

    }

    private void _testProjectDeleted()
    {
        getNavigator().gotoPage("/secure/project/ViewProjects.jspa", true);
        client.click("delete_project_10040", true);
        client.click("Delete", true);

        openCreateIssueDialog();
        checkQuickPID(PROJECT_LIST_NO_S, ALL_PROJECTS);
        recentProjects.remove("TPS");
        checkQuickPID(recentProjects, RECENT_PROJECTS);
    }

    private void _testDeleteProjectWhileDropdownOpen()
    {
        Window.openAndSelect(client, "", "secondary");

        openCreateIssueDialog();
        client.select("quick-pid", "label=TPB");

        client.selectWindow("secondary");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        client.click("delete_project_10003", true);
        client.click("Delete", true);

        Window.close(client, "secondary");
        client.click(CREATE_BUTTON, true);

        assertThat.textPresent("Create");
    }

    private void _testDeleteIssueTypeWhileDropdownOpen()
    {
        openCreateIssueDialog();
        client.select("quick-pid", "label=TPD");

        Window.openAndSelect(client, "", "secondary");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
        getNavigator().gotoAdmin();
        client.click("issue_types", true);
        client.click("//a[@href='DeleteIssueType!default.jspa?id=5']", true);
        client.click("Delete", true);
        Window.close(client, "secondary");
        client.click(CREATE_BUTTON, true);

        assertThat.textPresentByTimeout("The issue type selected is invalid.", TIMEOUT);

    }

    public void testNoDefault()
    {
        restoreData("TestCreateIssueMenuNoDefault.xml");

        openCreateIssueDialog();
        client.select("quick-pid", "label=No Default");
        assertEquals(client.getSelectedValue("quick-issuetype"), "-1");
        client.select("quick-pid", "label=TPD");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Bug"));
        client.select("quick-pid", "label=No Default");
        assertEquals(client.getSelectedValue("quick-issuetype"), "-1");

        client.click(CREATE_BUTTON);

        assertThat.visibleByTimeout("invalid-type", 10000);

        client.select("quick-pid", "label=TPD");
        assertEquals(client.getSelectedValue("quick-issuetype"), ISSUE_NAME_TO_ID.get("Bug"));
        assertThat.elementNotVisible("invalid-type");
    }

    private void openCreateIssueDialog()
    {
        if(client.isElementPresent("leave_admin"))
        {
            client.click("leave_admin", true);
        }
        client.click(CREATE_ISSUE_LINK);
        assertThat.visibleByTimeout(CREATE_BUTTON, TIMEOUT);
    }

}
