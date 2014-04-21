package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugin.issuenav.client.IssueWebPanels;
import com.atlassian.jira.plugin.issuenav.client.IssueWebPanelsClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.List;

/**
 * Test security around the ViewIssueWebPanels resource
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST })
public class TestViewIssueWebPanels extends RestFuncTest
{

    private IssueWebPanelsClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        client = new IssueWebPanelsClient(getEnvironmentData());
        administration.restoreData("TestViewIssueWebPanels.xml");
    }

    public void testErrorConditions()
    {
        Response response = client.getResponseForIssue("99999");
        assertEquals(404, response.statusCode);

        response = client.loginAs("fry").getResponseForIssue("10000");
        assertEquals(403, response.statusCode);

        response = client.anonymous().getResponseForIssue("10000");
        assertEquals(401, response.statusCode);
    }

    public void testSuccess()
    {
        IssueWebPanels panels = client.getPanelsForIssue("10000");

        assertHasKeys(panels.leftPanels,
                "com.atlassian.jira.jira-view-issue-plugin:details-module",
                "com.atlassian.jira.jira-view-issue-plugin:descriptionmodule",
                "com.atlassian.jira.jira-view-issue-plugin:activitymodule",
                "com.atlassian.jira.jira-view-issue-plugin:addcommentmodule");

        assertHasKeys(panels.rightPanels,
                "com.atlassian.jira.jira-view-issue-plugin:peoplemodule",
                "com.atlassian.jira.jira-view-issue-plugin:datesmodule");

        assertTrue(panels.infoPanels.isEmpty());
    }

    private static void assertHasKeys(List<IssueWebPanels.IssueWebPanel> panels, String... keys)
    {
        assertTrue(keys.length <= panels.size());
        for (int i = 0; i < keys.length; i++)
        {
            assertEquals(keys[i], panels.get(i).completeKey);
        }
    }
}
