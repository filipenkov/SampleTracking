package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Resolution;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.ResolutionClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

/**
 * Func test for the resolution resource.
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestResolutionResource extends RestFuncTest
{
    private ResolutionClient resolutionClient;

    public void testViewResolution() throws Exception
    {
        Resolution resolution = resolutionClient.get("2");
        assertEquals(getBaseUrlPlus("rest/api/2.0.alpha1/resolution/2"), resolution.self);
        assertEquals("The problem described is an issue which will never be fixed.", resolution.description);
        assertEquals("Won't Fix", resolution.name);
    }

    public void testViewResolutionNotFound() throws Exception
    {
        Response resp999 = resolutionClient.getResponse("999");
        assertEquals(404, resp999.statusCode);
        assertEquals(1, resp999.entity.errorMessages.size());
        assertTrue(resp999.entity.errorMessages.contains("The resolution with id '999' does not exist"));

        Response respBoom = resolutionClient.getResponse("boom");
        assertEquals(404, respBoom.statusCode);
        assertEquals(1, respBoom.entity.errorMessages.size());
        assertTrue(respBoom.entity.errorMessages.contains("The resolution with id 'boom' does not exist"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        resolutionClient = new ResolutionClient(getEnvironmentData());
        administration.restoreBlankInstance();
    }
}
