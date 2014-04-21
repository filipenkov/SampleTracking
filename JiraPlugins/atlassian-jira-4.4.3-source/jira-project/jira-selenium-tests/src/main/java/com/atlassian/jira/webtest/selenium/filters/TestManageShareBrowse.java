package com.atlassian.jira.webtest.selenium.filters;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import com.atlassian.selenium.Browser;
import com.atlassian.selenium.SkipInBrowser;
import junit.framework.Test;

/**
 * A test for the Manage Filters browse sharing select field
 *
 * @since v3.13
 */
@SkipInBrowser(browsers={Browser.IE}) //Element not found - Responsibility: JIRA Team
@WebTest({Category.SELENIUM_TEST })
public class TestManageShareBrowse extends JiraSeleniumTest
{
    private static final String PROJECT = "Project";
    private static final String GROUP = "Group";
    private static final String ANYONE = "Anyone";
    private static final String JIRA_ADMINISTRATORS = "jira-administrators";
    private static final String JIRA_DEVELOPERS = "jira-developers";
    private static final String JIRA_USERS = "jira-users";
    private static final String ALL = "All";
    private static final String HOMOSAPIEN = "homosapien";
    private static final String MONKEY = "monkey";
    private static final String ADMINISTRATORS = "Administrators";
    private static final String USERS = "Users";
    
    private static final String TYPE_DASHBOARDS = "dashboards";
    private static final String TYPE_FILTERS = "filters";

    public static Test suite()
    {
        return suiteFor(TestManageShareBrowse.class);
    }

    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestManageFilterBrowse.xml");
        getNavigator().login(ADMIN_USERNAME, ADMIN_PASSWORD).gotoHome();
    }
// TODO add back once convert Manage Dashboards
//    public void testSelectionInteractionsForDashboards() throws Exception
//    {
//        performTest("secure/ConfigurePortalPages!default.jspa?view=search", TYPE_DASHBOARDS);
//    }

    public void testSelectionInteractionsForFilters() throws Exception
    {
        performTest("secure/ManageFilters.jspa?filterView=search", TYPE_FILTERS);
    }

    private void performTest(final String url, final String entityType)
    {
        getNavigator().gotoPage(url, true);

        //
        // assert what the sharing select looks like
        assertSharingLooksLike(entityType, ANYONE);

        // perform a group search with default
        setSearch(GROUP);
        assertDescription(entityType, GROUP, JIRA_ADMINISTRATORS, null);
        executeSearch();
        assertSharingLooksLike(entityType, GROUP, JIRA_ADMINISTRATORS);

        // perform a group search with a specified values
        setSearch(GROUP, JIRA_DEVELOPERS);
        assertDescription(entityType, GROUP, JIRA_DEVELOPERS, null);
        executeSearch();
        assertSharingLooksLike(entityType, GROUP, JIRA_DEVELOPERS);

        setSearch(GROUP, JIRA_USERS);
        assertDescription(entityType, GROUP, JIRA_USERS, null);
        executeSearch();
        assertSharingLooksLike(entityType, GROUP, JIRA_USERS);

        // perform a project search with a default values
        setSearch(PROJECT);
        assertDescription(entityType, PROJECT, HOMOSAPIEN, ALL);
        executeSearch();
        assertSharingLooksLike(entityType, PROJECT, HOMOSAPIEN, ALL);

        setSearch(PROJECT, MONKEY);
        assertDescription(entityType, PROJECT, MONKEY, ALL);
        executeSearch();
        assertSharingLooksLike(entityType, PROJECT, MONKEY, ALL);

        setSearch(PROJECT, HOMOSAPIEN, ADMINISTRATORS);
        assertDescription(entityType, PROJECT, HOMOSAPIEN, ADMINISTRATORS);
        executeSearch();
        assertSharingLooksLike(entityType, PROJECT, HOMOSAPIEN, ADMINISTRATORS);

        setSearch(PROJECT, HOMOSAPIEN, USERS);
        assertDescription(entityType, PROJECT, HOMOSAPIEN, USERS);
        executeSearch();
        assertSharingLooksLike(entityType, PROJECT, HOMOSAPIEN, USERS);
    }

    private void setSearch(final String searchShareType)
    {
        setSearch(searchShareType, null, null);
    }

    private void setSearch( final String searchShareType, final String param1)
    {
        setSearch(searchShareType, param1, null);
    }

    private void setSearch(final String searchShareType, final String param1, final String param2)
    {
        if (GROUP.equalsIgnoreCase(searchShareType))
        {
            client.select("name=searchShareType", "label=Group");
            if (param1 != null)
            {
                client.select("name=groupShare", "label=" + param1);
            }
        }
        else if (PROJECT.equalsIgnoreCase(searchShareType))
        {
            client.select("name=searchShareType", "label=Project");
            if (param1 != null)
            {
                client.select("name=projectShare", "label=" + param1);
                if (param2 != null)
                {
                    client.select("name=roleShare", "label=" + param2);
                }
                else
                {
                    client.select("name=roleShare", "label=All");
                }
            }
        }
        else if (ANYONE.equalsIgnoreCase(searchShareType))
        {
            client.select("name=searchShareType", "label=Anyone");
        }
        else
        {
            fail("what the hell is that searchShareType");
        }
        executeSearch();
    }

    private void executeSearch()
    {
        client.click("Search");
        //client.waitForPageToLoad(PAGE_LOAD_WAIT);
    }

    private void assertSharingLooksLike(final String entityType, final String searchShareType)
    {
        assertSharingLooksLike(entityType, searchShareType, null, null);
    }

    private void assertSharingLooksLike(final String entityType, final String searchShareType, final String param1)
    {
        assertSharingLooksLike(entityType, searchShareType, param1, null);
    }

    private void assertSharingLooksLike(final String entityType, final String searchShareType, final String param1, final String param2)
    {
        assertThat.elementVisible("name=searchShareType");
        assertEquals(searchShareType, client.getSelectedLabel("name=searchShareType"));
        if (GROUP.equalsIgnoreCase(searchShareType))
        {
            assertEquals(param1, client.getSelectedLabel("name=groupShare"));
            //
            // and some things should not be seen
            assertThat.elementVisible("name=groupShare");
            assertThat.elementNotVisible("name=projectShare");
            assertThat.elementNotVisible("name=roleShare");
        }
        else if (PROJECT.equalsIgnoreCase(searchShareType))
        {
            assertEquals(param1, client.getSelectedLabel("name=projectShare"));
            if (param2 != null)
            {
                assertEquals(param2, client.getSelectedLabel("name=roleShare"));
            }
            else
            {
                assertEquals(ALL, client.getSelectedLabel("name=roleShare"));
            }
            //
            // and some things should not be seen
            assertThat.elementVisible("name=projectShare");
            assertThat.elementVisible("name=roleShare");
            assertThat.elementNotVisible("name=groupShare");
        }
        else if (ANYONE.equalsIgnoreCase(searchShareType))
        {
            //
            // and some things should not be seen
            assertThat.elementNotVisible("name=groupShare");
            assertThat.elementNotVisible("name=projectShare");
            assertThat.elementNotVisible("name=roleShare");
        }
        else
        {
            fail("what the hell is that searchShareType");
        }
        assertDescription(entityType, searchShareType, param1, param2);
    }

    private void assertDescription(String entityType, String searchShareType, String param1, String param2)
    {
        final String description;
        if (GROUP.equalsIgnoreCase(searchShareType))
        {
            description = "All " + entityType + " shared with the group '" + param1 + "'.";
        }
        else if (PROJECT.equalsIgnoreCase(searchShareType))
        {
            if (param2 != null && !param2.equalsIgnoreCase(ALL))
            {
                description = "All " + entityType + " shared with the project role '" + param2 + "' in the project '" + param1 + "'.";
            }
            else
            {
                description = "All " + entityType + " shared with either the project '" + param1 + "' or its roles.";
            }
        }
        else if (ANYONE.equalsIgnoreCase(searchShareType))
        {
            description = "All " + entityType + " that you can see.";
        }
        else
        {
            description = null;
            fail("what the hell is that searchShareType");
        }

        assertThat.textPresentByTimeout(description, 20000);
        assertEquals("Generated description is not correct.", description, client.getText("id=share_type_description"));
    }

}
