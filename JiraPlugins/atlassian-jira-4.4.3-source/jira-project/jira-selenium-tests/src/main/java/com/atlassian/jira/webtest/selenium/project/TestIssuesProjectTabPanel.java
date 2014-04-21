package com.atlassian.jira.webtest.selenium.project;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;
import junit.framework.Test;

@WebTest({Category.SELENIUM_TEST })
public class TestIssuesProjectTabPanel extends JiraSeleniumTest
{
    private static final String FRAG_UNRESOLVED_BY_ASSIGNEE_DIV = "//div[@id='fragunresolvedissuesbyassignee']";
    private static final String ASSIGNEES_TABLE = "//table[@id='assignees']";
    private static final String ASSIGNEES_TOGGLE_DIV = "//div[@id='fragunresolvedissuesbyassignee_toggle']";

    private static final String FRAG_UNRESOLVED_BY_COMPONENT_DIV = "//div[@id='fragunresolvedissuesbycomponent']";
    private static final String COMPONENTS_TOGGLE_DIV = "//div[@id='fragunresolvedissuesbycomponent_toggle']";

    private static final String FRAG_UNRESOLVED_BY_VERSION_DIV = "//div[@id='fragunresolvedissuesbyfixversion']";
    private static final String VERSIONS_TOGGLE_DIV = "//div[@id='fragunresolvedissuesbyfixversion_toggle']";

    private static final String ADMIN = "admin";

    public static Test suite()
    {
        return suiteFor(TestIssuesProjectTabPanel.class);
    }

    public void testUnresolvedIssuesByAssignee()
    {
        restoreData("TestIssuesProjectTabPanel_UnresolvedIssuesByAssignee.xml");
        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project%3Aissues-panel", true);

        // initially, toggle should be in "Show all/and X more" state
        assertEquals("and 1 more", client.getText(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TOGGLE_DIV));

        // also, only 10 rows should be visible
        assertNumberOfRowsVisible(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TABLE, 10);

        // toggle all rows to be displayed
        client.click(ASSIGNEES_TOGGLE_DIV);
        assertEquals("show first 10", client.getText(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TOGGLE_DIV));
        assertNumberOfRowsVisible(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TABLE, 11);

        // toggle back to initial state
        client.click(ASSIGNEES_TOGGLE_DIV);
        assertEquals("and 1 more", client.getText(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TOGGLE_DIV));
        assertNumberOfRowsVisible(FRAG_UNRESOLVED_BY_ASSIGNEE_DIV + ASSIGNEES_TABLE, 10);
    }

    public void testUnresolvedIssuesByComponent()
    {
        restoreData("TestIssuesProjectTabPanel_UnresolvedIssuesByAssignee.xml");
        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project%3Aissues-panel", true);

        // initially, toggle should be in "Show all/and X more" state
        assertEquals("and 1 more", client.getText(FRAG_UNRESOLVED_BY_COMPONENT_DIV + COMPONENTS_TOGGLE_DIV));

        // also, only 10 rows should be visible
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_COMPONENT_DIV, 10);

        // toggle all rows to be displayed
        client.click(COMPONENTS_TOGGLE_DIV);
        assertEquals("show first 10", client.getText(FRAG_UNRESOLVED_BY_COMPONENT_DIV + COMPONENTS_TOGGLE_DIV));
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_COMPONENT_DIV, 11);

        // toggle back to initial state
        client.click(COMPONENTS_TOGGLE_DIV);
        assertEquals("and 1 more", client.getText(FRAG_UNRESOLVED_BY_COMPONENT_DIV + COMPONENTS_TOGGLE_DIV));
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_COMPONENT_DIV, 10);
    }

    public void testUnresolvedIssuesByVersion()
    {
        restoreData("TestIssuesProjectTabPanel_UnresolvedIssuesByVersion.xml");
        safeLogin(ADMIN, ADMIN);
        getNavigator().gotoPage("browse/HSP?selectedTab=com.atlassian.jira.plugin.system.project%3Aissues-panel", true);

        // initially, toggle should be in "Show all/and X more" state
        assertEquals("and 2 more", client.getText(FRAG_UNRESOLVED_BY_VERSION_DIV + VERSIONS_TOGGLE_DIV));

        // also, only 10 rows should be visible
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_VERSION_DIV, 10);

        // toggle all rows to be displayed
        client.click(VERSIONS_TOGGLE_DIV);
        assertEquals("show first 10", client.getText(FRAG_UNRESOLVED_BY_VERSION_DIV + VERSIONS_TOGGLE_DIV));
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_VERSION_DIV, 12);

        // toggle back to initial state
        client.click(VERSIONS_TOGGLE_DIV);
        assertEquals("and 2 more", client.getText(FRAG_UNRESOLVED_BY_VERSION_DIV + VERSIONS_TOGGLE_DIV));
        assertNumberOfDivsVisible(FRAG_UNRESOLVED_BY_VERSION_DIV, 10);
    }

    private void assertNumberOfRowsVisible(String tableSelector, int count)
    {
        // first count rows should be visible
        for (int i = 1; i <= count; i++)
        {
            String rowSelect = tableSelector + "/tbody/tr[" + i + "]";
            assertTrue(client.isVisible(rowSelect));
        }

        String badRowSelect = tableSelector + "/tbody/tr[" + (count + 1) + "]";
        if (client.isElementPresent(badRowSelect))
        {
            assertFalse(client.isVisible(badRowSelect));
        }
    }

    private void assertNumberOfDivsVisible(String bodySelector, int count)
    {
        // first count divs should be visible
        for (int i = 1; i <= count; i++)
        {
            // note: select span to avoid the toggle div from also being selected
            String divSelect = bodySelector + "//div[" + i + "]/span";
            assertTrue(client.isVisible(divSelect));
        }

        String badDiv = bodySelector + "//div[" + (count + 1) + "]/span";
        if (client.isElementPresent(badDiv))
        {
            assertFalse(client.isVisible(badDiv));
        }
    }

    private void safeLogin(final String username, final String password)
    {
        //we have to logout to ensure that the session is clean for the next test. The tabs on managefilters is
        //remembered.
        getNavigator().logout(getXsrfToken());
        getNavigator().login(username, password);
    }
}
