package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Status;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.StatusClient;

/**
 * Func tests for StatusResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestStatusResource extends RestFuncTest
{
    /**
     * The id of a status that is visible by admin, but not by fred.
     */
    private static final String STATUS_ID = "10000";

    private StatusClient statusClient;

    /**
     * Verifies that the user is able to retrieve a visible status.
     *
     * @throws Exception if anything goes wrong
     */
    public void testStatusReturned() throws Exception
    {
        // this is what we expect:
        //
        // {
        //   "self": "http://localhost:8090/jira/rest/api/2.0.alpha1/status/10000",
        //   "description": "Custom status",
        //   "iconUrl": "http://localhost:8090/jira/images/icons/status_generic.gif",
        //   "name": "Insane"
        // }

        Status status = statusClient.get(STATUS_ID);
        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/status/10000"), status.self);
        assertEquals("Custom status", status.description);
        assertEquals(getBaseUrlPlus("images/icons/status_generic.gif"), status.iconUrl);
        assertEquals("Insane", status.name);
    }

    /**
     * Verifies that the user is not able to see a status that is not active on any of his projects.
     *
     * @throws Exception if anything goes wrong
     */
    public void testStatusFilteredByPermissions() throws Exception
    {
        Response response = statusClient.loginAs(FRED_USERNAME).getResponse(STATUS_ID);
        assertEquals(404, response.statusCode);
    }

    public void testStatusDoesntExist() throws Exception
    {
        // {"errorMessages":["The status with id '123' does not exist"],"errors":[]}
        Response resp123 = statusClient.getResponse("123");
        assertEquals(404, resp123.statusCode);
        assertEquals("The status with id '123' does not exist", resp123.entity.errorMessages.get(0));

        // {"errorMessages":["The status with id 'abc' does not exist"],"errors":[]}
        Response respAbc = statusClient.getResponse("abc");
        assertEquals(404, respAbc.statusCode);
        assertEquals("The status with id 'abc' does not exist", respAbc.entity.errorMessages.get(0));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        statusClient = new StatusClient(getEnvironmentData());
        administration.restoreData("TestStatusResource.xml");
    }
}
