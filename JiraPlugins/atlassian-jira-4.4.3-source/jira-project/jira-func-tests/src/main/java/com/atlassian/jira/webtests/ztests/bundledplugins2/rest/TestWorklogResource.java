package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Worklog;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.WorklogClient;

/**
 * Func test for Worklog resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.WORKLOGS })
public class TestWorklogResource extends RestFuncTest
{
    private WorklogClient worklogClient;

    public void testViewWorklog() throws Exception
    {
        Worklog worklog = worklogClient.get("10000");

        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/worklog/10000"), worklog.self);
        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/issue/HSP-1"), worklog.issue);
        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/user?username=admin"), worklog.author.self);
        assertEquals(ADMIN_USERNAME, worklog.author.name);
        assertEquals(ADMIN_FULLNAME, worklog.author.displayName);
        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/user?username=admin"), worklog.updateAuthor.self);
        assertEquals(ADMIN_USERNAME, worklog.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, worklog.updateAuthor.displayName);
        assertEquals("", worklog.comment);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", worklog.created);
        assertEqualDateStrings("2010-05-24T09:52:41.092+1000", worklog.updated);
        assertEqualDateStrings("2010-05-24T09:52:00.000+1000", worklog.started);
        assertEquals(120L, worklog.minutesSpent);
    }

    public void testViewWorklogNotFound() throws Exception
    {
        // {"errorMessages":["Cannot find worklog with id: '123'."],"errors":[]}
        Response response123 = worklogClient.getResponse("123");
        assertEquals(404, response123.statusCode);
        assertEquals(1, response123.entity.errorMessages.size());
        assertEquals("Cannot find worklog with id: '123'.", response123.entity.errorMessages.get(0));

        // {"errorMessages":["Cannot find worklog with id: 'abc'."],"errors":[]}
        Response responseAbc = worklogClient.getResponse("abc");
        assertEquals(404, responseAbc.statusCode);
        assertEquals(1, responseAbc.entity.errorMessages.size());
        assertEquals("Cannot find worklog with id: 'abc'.", responseAbc.entity.errorMessages.get(0));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        worklogClient = new WorklogClient(getEnvironmentData());
        administration.restoreData("TestWorklogAndTimeTracking.xml");
    }
}
