package com.atlassian.jira.webtest.selenium.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * Tests the browse projects page for the right behaviour involving project categories and project history
 *
 * @since v4.0
 */
@WebTest({Category.SELENIUM_TEST })
public class TestBrowseProjects extends JiraSeleniumTest
{
    private static final long TIMEOUT = 30000;
    private static final String BROWSE_PROJECTS_PAGE = "/secure/BrowseProjects.jspa";

    private static final String NO_CATEGORY_TAB = "none-panel-tab";
    private static final String NO_CATEGORY_PANEL = "none-panel";
    private static final String NO_CATEGORY_LINK = "none-panel-tab-lnk";

    private static final String ALL_PROJECTS_TAB = "all-panel-tab";
    private static final String ALL_PROJECTS_LINK = "all-panel-tab-lnk";

    private static final String RECENT_PROJECTS_TAB = "recent-panel-tab";
    private static final String RECENT_PROJECTS_PANEL = "recent-panel";
    private static final String RECENT_PROJECTS_LINK = "recent-panel-tab-lnk";

    private static final String CAT_A = "10000";
    private static final String CAT_A_PANEL = CAT_A + "-panel";
    private static final String CAT_A_TAB = CAT_A_PANEL + "-tab";
    private static final String CAT_A_LINK = CAT_A_TAB + "-lnk";
    private static final String CAT_B = "10010";
    private static final String CAT_B_PANEL = CAT_B + "-panel";
    private static final String CAT_B_TAB = CAT_B_PANEL + "-tab";
    private static final String CAT_B_LINK = CAT_B_TAB + "-lnk";
    private static final String CAT_C = "10011";
    private static final String CAT_C_PANEL = CAT_C + "-panel";
    private static final String CAT_C_TAB = CAT_C_PANEL + "-tab";
    private static final String CAT_C_LINK = CAT_C_TAB + "-lnk";

    
    private static final String ADD_CATEGORY_PAGE = "/secure/admin/projectcategories/ViewProjectCategories!default.jspa";
    private static final String ADD_PROJECT_PAGE = "secure/admin/AddProject!default.jspa";

    private static final String BROWSE_PROJECT_TAB_ITEM_XPATH = "//ul[@class='vertical tabs']/li[<INDEX>][@id='<ID>']";
    private static final String PROJECT_TABLE_ROW_XPATH = "jquery=#<PANEL> .projects-list tr:eq(<INDEX>) td:first";
    private static final String ACTIVE_TAB_XPATH = "//li[@class='active' and @id='<TAB_ID>']";
    private static final String FIRST_ACTIVE_TAB_XPATH = "//li[normalize-space(@class)='first active' and @id='<TAB_ID>']";

    private static final String NO_PROJECTS_ERROR = "There are no projects created.";


    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestBrowseProjectsPage.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD);
    }

    public void testBrowseProject()
    {
        _testNoProject();
        _testOneProject();
        _testNoCategories();
        _testNoCategoriesNoRecent();
        _testNoRecentOneCategory();
        _testRecentOneCategory();
        _testMultipleCategories();
        _testCategoriesWithNoProjects();
        _testCategoriesWithProjectsNoPermission();
        _testRecentProjects();
        _testTabSwitching();
        _testRememberLastTab();

    }

    private void _testNoProject()
    {
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.textPresentByTimeout(NO_PROJECTS_ERROR, TIMEOUT);
    }

    private void _testOneProject()
    {
        createProject("TPA", "TPA", ADMIN_USERNAME);
        getNavigator().login("test");
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        // If only one project present, browse projects goes straight to that project's page
        assertThat.textPresentByTimeout("TPA", TIMEOUT);
        assertThat.visibleByTimeout("content", TIMEOUT);

        // If only one project present, browse projects goes straight to that project's page, even if you have visited it
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.textPresentByTimeout("TPA", TIMEOUT);
        assertThat.visibleByTimeout("content", TIMEOUT);

        getNavigator().login("admin");
    }

    private void _testNoCategories()
    {
        // create multiple projects, but none associated to categories
        createProject("TPB", "TPB", ADMIN_USERNAME);
        createProject("TPC", "TPC", ADMIN_USERNAME);
        createProject("TPD", "TPD", ADMIN_USERNAME);
        createProject("TPE", "TPE", ADMIN_USERNAME);
        createProject("TPF", "TPF", ADMIN_USERNAME);


        getNavigator().login("test");

        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        //We should end up on the browse project page, one tab.
        assertThat.textPresentByTimeout("Browse Projects", TIMEOUT);

        checkBrowseProjectTabsHas(RECENT_PROJECTS_TAB, ALL_PROJECTS_TAB);

        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPA");

        client.click(ALL_PROJECTS_LINK);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPA", "TPB", "TPC", "TPD", "TPE", "TPF");
        getNavigator().login("admin");
    }

    private void _testNoCategoriesNoRecent()
    {
        // Delete the current recent project (TPA)
        getNavigator().gotoPage("/secure/project/DeleteProject!default.jspa?pid=10000&returnUrl=ViewProjects.jspa", true);
        client.click("Delete", true);

        getNavigator().login("test");
        // Check that the category tab now appears
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        
        //We should end up on the browse project page, one tab.
        assertThat.textPresentByTimeout("Browse Projects", TIMEOUT);

        assertThat.elementNotPresentByTimeout(ALL_PROJECTS_TAB);
        assertThat.elementNotPresentByTimeout(RECENT_PROJECTS_TAB);

        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPC", "TPD", "TPE", "TPF");
        getNavigator().login("admin");
    }

    private void _testNoRecentOneCategory()
    {
        // Assign TPC to cat alpha
        assignProjectToCategory("10002", "10000");

        getNavigator().login("test");
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        checkBrowseProjectTabsHas(CAT_A_TAB, NO_CATEGORY_TAB, ALL_PROJECTS_TAB);

        // Check that each of the tabs have the right contents
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_A_PANEL, "TPC");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPD", "TPE", "TPF");

        // Should be at the category tab
        client.click(CAT_A_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_A_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(CAT_A_PANEL, "TPC");

        // Check the no category tab
        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPD", "TPE", "TPF");
        getNavigator().login("admin");
    }

    private void _testRecentOneCategory()
    {
        getNavigator().login("test");
        // Create some project history
        getNavigator().browseProject("TPB");

        // Verify that recent history tab now shows up
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        checkBrowseProjectTabsHas(RECENT_PROJECTS_TAB, CAT_A_TAB, NO_CATEGORY_TAB, ALL_PROJECTS_TAB);

        // Ensure tabs have correct contents
        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");

        client.click(CAT_A_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_A_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_A_PANEL, "TPC");

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPD", "TPE", "TPF");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");
        checkProjectsTableHas(CAT_A_PANEL, "TPC");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPD", "TPE", "TPF");
        getNavigator().login("admin");
    }

    private void _testMultipleCategories()
    {
        // Add some categories and associate TPD -> CAT B and TPE -> CAT C
        createProjectCategory("cat beta");
        createProjectCategory("cat charlie");

        assignProjectToCategory("10003", CAT_B);
        assignProjectToCategory("10004", CAT_C);

        getNavigator().login("test");

        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        // Ensure the tabs for the new categories are present and have the right contents
        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_A_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");

        client.click(CAT_A_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_A_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_A_PANEL, "TPC");

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_B_PANEL, "TPD");

        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_C_PANEL, "TPE");

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPF");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");
        checkProjectsTableHas(CAT_A_PANEL, "TPC");
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPF");
        getNavigator().login("admin");
    }

    private void _testCategoriesWithNoProjects()
    {
        // Create a category with no projects
        createProjectCategory("cat delta");

        getNavigator().login("test");
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_A_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        // Ensure that category does not appear
        assertThat.elementNotPresentByTimeout("//li[@id='10012-panel-tab']", TIMEOUT);

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");

        // Verify other tabs still have the right contents
        client.click(CAT_A_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_A_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_A_PANEL, "TPC");

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_B_PANEL, "TPD");

        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_C_PANEL, "TPE");

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPF");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPB");
        checkProjectsTableHas(CAT_A_PANEL, "TPC");
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPB", "TPF");
        getNavigator().login("admin");
    }

    private void _testCategoriesWithProjectsNoPermission()
    {
        // Remove browse permissions for TPB (no category) and TPC (cat alpha)
        getNavigator().gotoPage("/secure/project/SelectProjectPermissionScheme!default.jspa?projectId=10002", true);
        client.select("schemeIds_select", "value=10000");
        client.click("Associate", true);

        getNavigator().gotoPage("/secure/project/SelectProjectPermissionScheme!default.jspa?projectId=10001", true);
        client.select("schemeIds_select", "value=10000");
        client.click("Associate", true);

        getNavigator().login("test");
        // As a result, cat alpha should not appear. Also, project B was last visited so
        // removing browse permissions should remove recent projects
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        checkBrowseProjectTabsHas(CAT_B_TAB, CAT_C_TAB, NO_CATEGORY_TAB, ALL_PROJECTS_TAB);

        assertThat.elementNotPresentByTimeout(RECENT_PROJECTS_TAB, TIMEOUT);
        assertThat.elementNotPresentByTimeout(RECENT_PROJECTS_LINK, TIMEOUT);

        assertThat.elementNotPresentByTimeout(CAT_A_PANEL, TIMEOUT);
        assertThat.elementNotPresentByTimeout(CAT_A_LINK, TIMEOUT);

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(CAT_B_PANEL, "TPD");

        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);
        checkProjectsTableHas(CAT_C_PANEL, "TPE");

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPF");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        assertThat.elementNotPresentByTimeout(RECENT_PROJECTS_PANEL, TIMEOUT);
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPF");
        getNavigator().login("admin");
    }

    private void _testRecentProjects()
    {
        getNavigator().login("test");
        // Add some project history
        getNavigator().browseProject("TPD");
        getNavigator().browseProject("TPE");
        getNavigator().browseProject("TPF");

        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        // Ensure the history is reflected in both recent and all projects
        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPF", "TPE", "TPD");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPF", "TPE", "TPD");
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPF");

        // Change the order of project history and ensure it is reflected
        getNavigator().browseProject("TPE");

        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPE", "TPF", "TPD");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPE", "TPF", "TPD");
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPF");

        // Just to make sure, change the order yet again
        getNavigator().browseProject("TPD");

        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPD", "TPE", "TPF");

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        checkProjectsTableHas(RECENT_PROJECTS_PANEL, "TPD", "TPE", "TPF");
        checkProjectsTableHas(CAT_B_PANEL, "TPD");
        checkProjectsTableHas(CAT_C_PANEL, "TPE");
        checkProjectsTableHas(NO_CATEGORY_PANEL, "TPF");
        getNavigator().login("admin");
    }

    private void _testTabSwitching()
    {
        getNavigator().login("test");
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        checkBrowseProjectTabsHas(
                RECENT_PROJECTS_TAB,
                CAT_B_TAB,
                CAT_C_TAB,
                NO_CATEGORY_TAB,
                ALL_PROJECTS_TAB
        );

        // Go down the tab menu and up again
        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);

        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);

        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        getNavigator().login("admin");
    }

    private void _testRememberLastTab()
    {
        getNavigator().login("test");
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);

        client.click(ALL_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);
        getNavigator().issueNavigator().displayAllIssues();
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.elementPresentByTimeout(activeTabXpathFor(ALL_PROJECTS_TAB), TIMEOUT);

        client.click(NO_CATEGORY_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
        getNavigator().issueNavigator().displayAllIssues();
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.elementPresentByTimeout(activeTabXpathFor(NO_CATEGORY_TAB), TIMEOUT);
               
        client.click(CAT_C_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);
        getNavigator().issueNavigator().displayAllIssues();
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_C_TAB), TIMEOUT);

        client.click(CAT_B_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);
        getNavigator().issueNavigator().displayAllIssues();
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.elementPresentByTimeout(activeTabXpathFor(CAT_B_TAB), TIMEOUT);

        client.click(RECENT_PROJECTS_LINK);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        getNavigator().issueNavigator().displayAllIssues();
        getNavigator().gotoPage(BROWSE_PROJECTS_PAGE, true);
        assertThat.elementPresentByTimeout(activeTabXpathFor(RECENT_PROJECTS_TAB, FIRST_ACTIVE_TAB_XPATH), TIMEOUT);
        getNavigator().login("admin");
    }

    
    private void createProject(final String name, final String key, final String lead)
    {

        getNavigator().gotoPage(ADD_PROJECT_PAGE, true);
        client.typeInElementWithName("name", name);
        client.typeInElementWithName("key", key);
        client.typeInElementWithName("lead", lead);
        client.click("Add", true);
    }


    private void checkBrowseProjectTabsHas(final String... tabs)
    {
        for (int i = 0; i < tabs.length; ++i)
        {
            String tabToCheck = BROWSE_PROJECT_TAB_ITEM_XPATH
                    .replace("<INDEX>", Integer.toString(i + 1))
                    .replace("<ID>", tabs[i]);
            assertThat.elementPresentByTimeout(tabToCheck, TIMEOUT);
        }
    }

    private void checkProjectsTableHas(final String panel, final String... projects)
    {
        for (int i = 0; i < projects.length; ++i)
        {
            String rowToCheck = PROJECT_TABLE_ROW_XPATH
                    .replace("<PANEL>", panel)
                    .replace("<INDEX>", Integer.toString(i + 0));
//            assertThat.visibleByTimeout("jquery=#" + panel, TIMEOUT);
            assertThat.textPresentByTimeout(projects[i], TIMEOUT);
            assertThat.elementHasText(rowToCheck, projects[i]);
        }
    }

    private void createProjectCategory(final String category)
    {
        getNavigator().gotoPage(ADD_CATEGORY_PAGE, true);
        client.typeInElementWithName("name", category);
        client.click("Add", true);
    }

    private void assignProjectToCategory(final String project, final String category)
    {
        getNavigator().gotoPage("/secure/project/SelectProjectCategory!default.jspa?pid=" + project, true);
        client.select("pcid_select", "value=" + category);
        client.click("Select", true);

    }

    private String activeTabXpathFor(final String tabId){
        return activeTabXpathFor(tabId, ACTIVE_TAB_XPATH);
    }

    private String activeTabXpathFor(final String tabId, final String xPath){
        return xPath.replace("<TAB_ID>", tabId);
    }


}