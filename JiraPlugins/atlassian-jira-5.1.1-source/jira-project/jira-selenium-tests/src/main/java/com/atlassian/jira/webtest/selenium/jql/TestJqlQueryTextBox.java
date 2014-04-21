package com.atlassian.jira.webtest.selenium.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtest.selenium.JiraSeleniumTest;

/**
 * Test related to the Jql query box in the Issue Navigator advanced screen.
 *
 * @since v4.0
 */

@WebTest({Category.SELENIUM_TEST })
public class TestJqlQueryTextBox extends JiraSeleniumTest
{
    @Override
    public void onSetUp()
    {
        super.onSetUp();
        restoreData("TestJqlQueryTextBox.xml");
        getNavigator().gotoFindIssuesAdvanced();
    }

    public void testQueryHistoryBoxMaxItems() throws Exception
    {
        assertQueryHistory();

        final String query1 = "Issue = \"HSP-1\"";
        runJqlQuery(query1,"HSP-1");
        assertQueryHistory(query1);

        final String query2 = "resolution = \"Fixed\"";
        runJqlQuery(query2, "HSP-3");
        assertQueryHistory(query2, query1);

        final String query3 = "Issue = \"HSP-2\"";
        runJqlQuery(query3,"HSP-2");
        assertQueryHistory(query3, query2, query1);

        final String query4 = "component is empty";
        runJqlQuery(query4, "HSP-3","HSP-1");
        assertQueryHistory(query4, query3, query2, query1);

        final String query5 = "Issue != \"HSP-1\"";
        runJqlQuery(query5, "HSP-3", "HSP-2");
        assertQueryHistory(query5, query4, query3, query2, query1);

        final String query6 = "fixVersion is empty";
        runJqlQuery(query6, "HSP-3", "HSP-2", "HSP-1");
        assertQueryHistory(query6, query5, query4, query3, query2, query1);

        final String query7 = "Issue != \"HSP-2\"";
        runJqlQuery(query7, "HSP-3", "HSP-1");
        assertQueryHistory(query7, query6, query5, query4, query3, query2, query1);

        final String query8 = "Summary ~ \"A sample Issue\"";
        runJqlQuery(query8, "HSP-2", "HSP-1");
        assertQueryHistory(query8, query7, query6, query5, query4, query3, query2, query1);

        final String query9 = "affectedVersion is empty";
        runJqlQuery(query9, "HSP-3", "HSP-2", "HSP-1");
        assertQueryHistory(query9, query8, query7, query6, query5, query4, query3, query2, query1);

        final String query10 = "Assignee = fred";
        runJqlQuery(query10, "HSP-1");
        assertQueryHistory(query10, query9, query8, query7, query6, query5, query4, query3, query2, query1);

        final String query11 = "Assignee = administrator";
        runJqlQuery(query11, "HSP-3","HSP-2");
        assertQueryHistory(query11, query10, query9, query8, query7, query6, query5, query4, query3, query2);

        runJqlQueryFromHistory(1,"HSP-1");

        assertQueryHistory(query10, query11, query9, query8, query7, query6, query5, query4, query3, query2);
    }

    public void testQueryHistoryItemReplacesTextArea() throws Exception
    {
        String query1 = "project = homosapien";
        String query2 = "issue = \"HSP-1\"";
        String query3 = "issue = \"HSP-2\"";
        String query4 = "project = monkey";

        runJqlQuery(query1, "HSP-3", "HSP-2", "HSP-1");
        runJqlQuery(query2, "HSP-1");
        runJqlQuery(query3, "HSP-2");
        runJqlQuery(query4);
        assertQueryHistory(query4, query3, query2, query1);

        client.type("jqltext",query1 + " some garbage");

        runJqlQueryFromHistory(1,"HSP-2");
    }



    private void runJqlQuery(final String query, final String... issueKeys)
    {
        client.type("jqltext", query + " ORDER BY issuekey DESC");
        client.click("jqlrunquery");
        client.waitForPageToLoad();
        assertIssueKeys(issueKeys);
    }

    private void assertIssueKeys(final String... issueKeys)
    {
        if(issueKeys.length > 0)
        {
            assertThat.elementPresent("//table[@id = 'issuetable']");
            for (int i = 0; i < issueKeys.length; i++)
            {
                int rowNum = i+1;
                final String issueKey = client.getTable("issuetable."+rowNum+".1");
                assertEquals(issueKeys[i],issueKey);
            }
        }else
        {
            assertThat.elementNotPresent("//table[@id = 'issuetable']");
        }
    }

    private void assertQueryHistory(String... queryItems)
    {
        for (int i = 0; i < queryItems.length; i++)
        {
            assertThat.elementPresent("historyItem"+i);
            final String historyItem = client.getText("historyItem" + i);
            assertEquals(queryItems[i] + " ORDER BY issuekey DESC", historyItem);
        }
//        assertFalse("There are more JQL queries in the history than expected.", client.isElementPresent("historyItem"+ queryItems.length));
    }

    private void runJqlQueryFromHistory(int queryIndex, final String... issueKeys)
    {
        client.click("historyItem" + queryIndex);
        client.waitForPageToLoad();
        assertIssueKeys(issueKeys);
    }

}
