package com.atlassian.jira.user.util;

import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.user.OfbizExternalEntityStore;

import java.util.List;

/**
 * Unit test for UserUtilImpl
 */
public class TestGlobalUserPreferencesUtil extends LegacyJiraMockTestCase
{
    private GlobalUserPreferencesUtil userUtil;
    private MockOfBizDelegator mockOfBizDelegator;

    public TestGlobalUserPreferencesUtil(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        mockOfBizDelegator = new MockOfBizDelegator(null, null);
        userUtil = new GlobalUserPreferencesUtilImpl(mockOfBizDelegator, null);
    }

    public void testTotalCountExternalWithNoLocalUsers()
    {
        List gvs = EasyList.build(createMockGenericValue(new Long(1), "ExternalEntity", "html"),
                new MockGenericValue("ExternalEntity", EasyMap.build("id", new Long(1), "name", "fred", "type", OfbizExternalEntityStore.class.getName())));

        mockOfBizDelegator.setGenericValues(gvs);

        //There's no local users.
        long userCount = userUtil.getTotalUpdateUserCountMailMimeType("text");
        assertEquals("Total count", 1, userCount);


        // all users already have html set.
        userCount = userUtil.getTotalUpdateUserCountMailMimeType("html");
        assertEquals("Total count", 0, userCount);
    }


    private MockGenericValue createMockGenericValue(Long id, String entityName, String propertyValue)
    {
        return new MockGenericValue("OSUserPropertySetView", EasyMap.build("entityName", entityName, "entityId", id, "propertyValue", propertyValue, "propertyKey", "user.notifications.mimetype"));
    }
}

