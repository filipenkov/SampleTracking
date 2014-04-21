package it.com.atlassian.jira.plugin.issuenav.func;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.plugin.issuenav.client.SearchRendererClient;
import com.atlassian.jira.plugin.issuenav.service.SearchRendererValue;
import com.atlassian.jira.plugin.issuenav.service.SearchRendererValueResults;
import com.atlassian.jira.plugin.issuenav.service.SearchResults;
import com.atlassian.jira.plugin.issuenav.util.EditHtmlUtils;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.RestFuncTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.Map;

/**
 * Test security and response for the search renderer resource
 *
 * @since v5.1
 */
@WebTest ({ Category.FUNC_TEST })
public class TestSearchRenderer extends RestFuncTest
{
    private SearchRendererClient client;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        client = new SearchRendererClient(getEnvironmentData());
        administration.restoreBlankInstance();
    }

    public void testEditErrorConditions()
    {
        Response response = client.getEditHtmlResponse("zzz");
        assertEquals(400, response.statusCode);

        response = client.anonymous().getEditHtmlResponse("zzz");
        assertEquals(401, response.statusCode);

        // TODO: further security checks? See JRADEV-9228
    }

    public void testEditHtmlSuccess()
    {
        String editHtml = client.getEditHtml("project");

        // Asserts that some representation of projects comes back from the server
        assertTrue(editHtml.contains("homosapien"));
        assertTrue(editHtml.contains("monkey"));
    }

    public void testValueErrorConditions()
    {
//        Response response = client.getValueResponse("pid", "99999");
//        assertEquals(400, response.statusCode);

        // TODO: further security checks? See JRADEV-9228
//        response = client.loginAs("fry").getValueResponse("10000", "pid", "hi");
//        assertEquals(403, response.statusCode);
//
//        response = client.anonymous().getValueResponse("10000", "pid", "hi");
//        assertEquals(401, response.statusCode);
    }

    public void testValueSuccess()
    {
        SearchRendererValueResults value = client.getValue("pid", "10000");

        // Asserts that some representation of projects comes back from the server
        assertEquals(value.size(), 1);
        assertTrue(value.containsKey("project"));
        SearchRendererValue project = value.get("project");

        assertEquals(project.name, "Project");

        assertTrue(project.viewHtml.contains("homosapien"));
        assertFalse(project.viewHtml.contains("monkey"));

        // Asserts that some representation of edit html comes back from the server
        assertTrue(project.editHtml.contains("homosapien"));
        assertTrue(project.editHtml.contains("monkey"));
        
        assertEquals("project = HSP", project.jql);
        EditHtmlUtils.assertSelectHtmlValues(project.editHtml, "10000");
    }

    public void testMultiValueSuccess()
    {
        SearchRendererValueResults value = client.getValue("pid", "10000", "type", "1");

        // Asserts that some representation of projects comes back from the server
        assertEquals(value.size(), 2);
        assertTrue(value.containsKey("project"));
        assertTrue(value.containsKey("issuetype"));

        SearchRendererValue project = value.get("issuetype");
        assertTrue(project.viewHtml.contains("Bug"));

        assertEquals("issuetype = Bug", project.jql);
        EditHtmlUtils.assertSelectHtmlValues(project.editHtml, "1");
    }

    public void testMultiValueSuccessWithCompoundDateFields()
    {
        SearchRendererValueResults value = client.getValue("created:after", "7/Mar/12", "created:before", "14/Mar/12");

        // Asserts that some representation of projects comes back from the server
        assertEquals(value.size(), 1);
        assertTrue(value.containsKey("created"));

        SearchRendererValue createdBefore = value.get("created");
        assertTrue(createdBefore.viewHtml.contains("Created Before"));
        assertTrue(createdBefore.viewHtml.contains("Created After"));

        assertEquals("created >= 2012-03-07 AND created <= 2012-03-14", createdBefore.jql);
        EditHtmlUtils.assertDatePickerHtmlValues(createdBefore.editHtml, "14/Mar/12", "7/Mar/12");
    }

    public void testValueEmpty()
    {
        Map<String, ?> value = client.getValue();

        assertEquals(value.size(), 0);
    }

    public void testValueNotRecognized()
    {
        Map<String, ?> value = client.getValue("unrecognized", "value");

        assertEquals(value.size(), 0);
    }
    
    public void testValueSuccessWithComponent()
    {
        SearchRendererValueResults value = client.getValue("pid", "10000", "component", "10000");
        
        assertEquals(2, value.size());
        assertTrue(value.containsKey("project"));
        assertTrue(value.containsKey("component"));

        SearchRendererValue component = value.get("component");
        assertEquals(component.name, "Components");
        assertTrue(component.viewHtml.contains("New Component 1"));
        assertTrue(component.editHtml.contains("New Component 1"));
        assertTrue(component.validSearcher);

        assertEquals("component = \"New Component 1\"", component.jql);
        EditHtmlUtils.assertSelectHtmlValues(component.editHtml, "10000");
    }

    /**
     * Tests case where user has selected project, then selected component, then unselected project
     */
    public void testValueWithComponentForInvalidProject()
    {
        SearchRendererValueResults value = client.getValue("component", "10000");

        assertEquals(1, value.size());
        assertTrue(value.containsKey("component"));

        SearchRendererValue component = value.get("component");
        assertNull(component.viewHtml);
        assertNull(component.editHtml);
        assertFalse(component.validSearcher);

        assertEquals("component = \"New Component 1\"", component.jql);
    }

    /**
     * Tests multi resource with no context
     */
    public void testMulti()
    {
        SearchResults results = client.getMulti();

        assertEquals(0, results.values.size());

        // Test that component, version are not returned for multi-project
        assertFalse("Component searcher is not returned for multi-project search", TestIssueSearcher.searcherExists(results.searchers, "component"));
        assertFalse("Version searcher is not returned for multi-project search", TestIssueSearcher.searcherExists(results.searchers, "version"));
    }

    public void testMultiWithContext()
    {
        SearchResults results = client.getMulti("pid", "10000");

        assertEquals(1, results.values.size());
        assertTrue(results.values.containsKey("project"));

        // Test that component, version are returned for project context
        assertTrue("Component searcher is returned for single project search", TestIssueSearcher.searcherExists(results.searchers, "component"));
        assertTrue("Version searcher is returned for single project search", TestIssueSearcher.searcherExists(results.searchers, "version"));
    }
}
