package com.atlassian.jira.web.action;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.mock.web.action.MockAbstractBrowser;
import com.atlassian.jira.web.SessionKeys;
import webwork.action.ActionContext;

import java.util.HashMap;
import java.util.Map;

public class TestAbstractBrowser extends LegacyJiraMockTestCase
{
    protected void tearDown() throws Exception
    {
        ActionContext.getSession().remove(SessionKeys.SEARCH_PAGER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_SORTER);
        ActionContext.getSession().remove(SessionKeys.SEARCH_REQUEST);
        super.tearDown();
    }

    public void testSetParameters()
    {
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("id", new String[] { "1" });
        params.put("version", new String[] { "10" });

        MockAbstractBrowser ab = new MockAbstractBrowser();
        ab.setParameters(params);

        Map returnParams = ab.getParameters();
        assertNotNull(returnParams);
        assertTrue(!returnParams.isEmpty());
        assertEquals(2, params.size());
    }

    public void testGetSingleParam()
    {
        HashMap<String, String[]> params = new HashMap<String, String[]>();
        params.put("id", new String[] { "1" });
        params.put("version", new String[] { "10" });

        MockAbstractBrowser ab = new MockAbstractBrowser();
        ab.setParameters(params);
        assertNull(ab.getSingleParam("ABC"));
        assertEquals("1", ab.getSingleParam("id"));
        assertEquals("10", ab.getSingleParam("version"));
    }

    public void testSetStart1()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        ab.setStart("1");
        assertEquals(1, ab.getPager().getStart());
    }

    public void testSetStart2()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        assertEquals(0, ab.getPager().getStart());
        ab.setStart("this is a string");
        assertEquals(0, ab.getPager().getStart());
    }

    public void testGetNiceStart()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        assertEquals(0, ab.getNiceStart());
        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", 1L));
        assertEquals(1, ab.getNiceStart());
        for (long i = 2; i <= 10; i++)
        {
            UtilsForTests.getTestEntity("Issue", EasyMap.build("id", i));
        }

        ab.setStart("5");
        assertEquals(6, ab.getNiceStart());
    }

    public void testGetNiceEnd()
    {
        MockAbstractBrowser ab = new MockAbstractBrowser();

        assertEquals(0, ab.getNiceEnd());

        UtilsForTests.getTestEntity("Issue", EasyMap.build("id", 1L));
        assertEquals(1, ab.getNiceEnd());
        for (long i = 2; i <= 10; i++)
        {
            UtilsForTests.getTestEntity("Issue", EasyMap.build("id", i));
        }

        ab.setStart("5");
        assertEquals(10, ab.getNiceEnd());
    }

}
