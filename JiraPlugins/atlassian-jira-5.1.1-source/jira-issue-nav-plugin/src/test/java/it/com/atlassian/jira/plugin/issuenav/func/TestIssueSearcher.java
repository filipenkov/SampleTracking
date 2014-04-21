package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugin.issuenav.client.IssueSearcherClient;
import com.atlassian.jira.plugin.issuenav.service.FilteredSearcherGroup;
import com.atlassian.jira.plugin.issuenav.service.Searcher;
import com.atlassian.jira.plugin.issuenav.service.Searchers;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;


/**
 * Test security and response for the issue searcher
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST })
public class TestIssueSearcher extends RestFuncTest
{
    private IssueSearcherClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        client = new IssueSearcherClient(getEnvironmentData());
        administration.restoreBlankInstance();
    }

    public void testSearchers()
    {
        // Test searching with no context
        Searchers searchers = client.getSearchers();

        // Test some basic fields
        assertTrue("Reporter searcher is returned", searcherExists(searchers, "reporter"));

        // Test that component, version are not returned for multi-project
        assertFalse("Component searcher is not returned for multi-project search", searcherExists(searchers, "component"));
        assertFalse("Version searcher is not returned for multi-project search", searcherExists(searchers, "version"));

        // Test that component, version are returned for project context
        searchers = client.getSearchers("project = homosapien");
        assertTrue("Component searcher is returned for single project search", searcherExists(searchers, "component"));
        assertTrue("Version searcher is returned for single project search", searcherExists(searchers, "version"));
    }

    public static boolean searcherExists(Searchers searchers, String id)
    {
        for (FilteredSearcherGroup group : searchers.getGroups())
        {
            for (Searcher searcher : group.getSearchers())
            {
                if (id.equals(searcher.getId()))
                {
                    return true;
                }
            }
        }
        return false;
    }
}
