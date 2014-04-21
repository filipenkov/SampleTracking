package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Priority;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.PriorityClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

/**
 * Func tests for PriorityResource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestPriorityResource extends RestFuncTest
{
    private PriorityClient priorityClient;

    public void testViewPriority() throws Exception
    {
        Priority priority = priorityClient.get("1");

        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/priority/1"), priority.self);
        assertEquals("#cc0000", priority.statusColor);
        assertEquals("Blocks development and/or testing work, production could not run.", priority.description);
        assertEquals(getBaseUrlPlus("images/icons/priority_blocker.gif"), priority.iconUrl);
        assertEquals("Blocker", priority.name);
    }

    public void testViewPriorityNotFound() throws Exception
    {
        // {"errorMessages":["The priority with id '123' does not exist"],"errors":[]}
        Response resp123 = priorityClient.getResponse("123");
        assertEquals(404, resp123.statusCode);
        assertEquals(1, resp123.entity.errorMessages.size());
        assertTrue(resp123.entity.errorMessages.contains("The priority with id '123' does not exist"));

        // {"errorMessages":["The priority with id 'foo' does not exist"],"errors":[]}
        Response respFoo = priorityClient.getResponse("foo");
        assertEquals(404, respFoo.statusCode);
        assertTrue(respFoo.entity.errorMessages.contains("The priority with id 'foo' does not exist"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        priorityClient = new PriorityClient(getEnvironmentData());
        administration.restoreBlankInstance();
    }
}
