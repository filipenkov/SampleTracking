package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLinkType;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLinkTypeClient;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.IssueLinkTypes;
import com.atlassian.jira.webtests.ztests.bundledplugins2.rest.client.Response;

import java.util.List;

/**
 * 
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueLinkTypeResource extends RestFuncTest
{
    private IssueLinkTypeClient issueLinkTypeClient;

    public void testGetAllIssueLinkTypes() throws Exception
    {
        final IssueLinkTypes issueLinkTypes = issueLinkTypeClient.getIssueLinkTypes();
        final List<IssueLinkType> list = issueLinkTypes.issueLinkTypes;
        assertEquals(2, list.size());
        IssueLinkType type = list.get(0);
        assertEquals(type.name, "Blocks");
        assertEquals("Blocks", type.outward);
        assertEquals("Blocked by", type.inward);
        assertEquals(new Long(10100).intValue(), type.id.intValue());
        assertEquals(getBaseUrlPlus("rest/api/2/issueLinkType/10100") ,type.self.toString());
        type = list.get(1);
        assertEquals(type.name, "Duplicate");
        assertEquals("Duplicates", type.outward);
        assertEquals("Duplicated by", type.inward);
        assertEquals(new Long(10000).intValue(), type.id.intValue());
        assertEquals(getBaseUrlPlus("rest/api/2/issueLinkType/10000") ,type.self.toString());
    }

    public void testGetAllIssueLinkTypesReturns404WhenIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        assertEquals(404, issueLinkTypeClient.getResponseForAllLinkTypes().statusCode);
    }

    public void testGetAllIssueLinkTypesAnonymousUserAllowed() throws Exception
    {
        final IssueLinkTypes issueLinkTypes = issueLinkTypeClient.anonymous().getIssueLinkTypes();
        final List<IssueLinkType> list = issueLinkTypes.issueLinkTypes;
        assertEquals(2, list.size());
    }

    public void testGetIssueLinkTypeReturns404WhenIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        assertEquals(404, issueLinkTypeClient.getResponseForLinkType("10000").statusCode);
    }

    public void testGetIssueLinkType() throws Exception
    {
        final IssueLinkType type = issueLinkTypeClient.anonymous().getIssueLinkType("10000");
        assertEquals(type.name, "Duplicate");
        assertEquals("Duplicates", type.outward);
        assertEquals("Duplicated by", type.inward);
        assertEquals(new Long(10000).intValue(), type.id.intValue());
    }

    public void testGetIssueLinkTypeIssueLinkTypeNotFound() throws Exception
    {
        final Response response = issueLinkTypeClient.getResponseForLinkType("10012");
        assertEquals(404, response.statusCode);
        assertEquals("No issue link type with id '10012' found.", response.entity.errorMessages.get(0));
    }



    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueLinkTypeClient = new IssueLinkTypeClient(getEnvironmentData());
        administration.restoreData("TestIssueLinkType.xml");
    }
}
