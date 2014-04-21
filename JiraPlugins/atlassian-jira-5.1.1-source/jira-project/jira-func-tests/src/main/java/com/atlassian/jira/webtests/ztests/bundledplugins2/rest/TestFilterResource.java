package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Filter;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.FilterClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.List;

@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestFilterResource extends RestFuncTest
{

    private FilterClient filterClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        filterClient = new FilterClient(getEnvironmentData());

        administration.restoreData("TestFilterResource.xml");
    }

    public void testAnonymous()
    {
        filterClient.anonymous();

        Response response = filterClient.getResponse("10000");
        assertEquals(400, response.statusCode);
        assertEquals("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.", response.entity.errorMessages.get(0));

        Filter json = filterClient.get("10002");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10002", json.self);
        assertEquals("10002", json.id);
        assertEquals("Public filter", json.name);
        assertEquals("Everyone can see this!", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        //anonymous should not see the jql.
        assertEquals(null, json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10002"), json.viewUrl);
        assertEquals(null, json.searchUrl);
        assertFalse(json.favourite);
    }

    public void testFilterJson() throws Exception
    {
        //here's a non favourite filter I created
        Filter json = filterClient.get("10000");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10000", json.self);
        assertEquals("10000", json.id);
        assertEquals("My new awesome filter!", json.name);
        assertEquals("And here's a description", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("type = Bug", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10000"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=type+%3D+Bug"), json.searchUrl);
        assertFalse(json.favourite);

        //here's a favourite filter I created
        json = filterClient.get("10001");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10001", json.self);
        assertEquals("10001", json.id);
        assertEquals("All Issues", json.name);
        assertEquals(null, json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10001"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql="), json.searchUrl);
        assertTrue(json.favourite);

        filterClient.loginAs("bob");
        //Shouldn't be able to get a filter that's not shared with me!
        Response response = filterClient.getResponse("10000");
        assertEquals(400, response.statusCode);
        assertEquals("The selected filter is not available to you, perhaps it has been deleted or had its permissions changed.", response.entity.errorMessages.get(0));

        //lets get a public filter created by someone else!
        json = filterClient.get("10002");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10002", json.self);
        assertEquals("10002", json.id);
        assertEquals("Public filter", json.name);
        assertEquals("Everyone can see this!", json.description);
        assertEquals("admin", json.owner.name);
        assertEquals("Administrator", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.owner.self);
        assertEquals("project = homosapien and type = bug", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10002"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=project+%3D+homosapien+and+type+%3D+bug"), json.searchUrl);
        assertTrue(json.favourite);

        //And finally get a filter Bob created
        json = filterClient.get("10003");
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/filter/10003", json.self);
        assertEquals("10003", json.id);
        assertEquals("This is bob's filter!", json.name);
        assertEquals(null, json.description);
        assertEquals("bob", json.owner.name);
        assertEquals("Bob Brown", json.owner.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=bob", json.owner.self);
        assertEquals("text ~ \"some fancy text\"", json.jql);
        assertEquals(getBaseUrlPlus("secure/IssueNavigator.jspa?mode=hide&requestId=10003"), json.viewUrl);
        assertEquals(getBaseUrlPlus("rest/api/2/search?jql=text+~+%22some+fancy+text%22"), json.searchUrl);
    }

    public void testGetFavouriteFilters() throws Exception
    {
        filterClient.anonymous();
        List<Filter> filters = filterClient.getFavouriteFilters();
        assertTrue(filters.isEmpty());

        filterClient.loginAs("admin");
        List<Filter> favouriteFilters = filterClient.getFavouriteFilters();
        assertEquals(2, favouriteFilters.size());
        assertEquals("10001", favouriteFilters.get(0).id);
        assertEquals("10002", favouriteFilters.get(1).id);
    }
}
