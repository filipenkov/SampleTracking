package com.atlassian.jira.issue.customfields.view;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;

import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.core.util.collection.EasyList;
import com.mockobjects.dynamic.Mock;
import com.atlassian.jira.local.ListeningTestCase;

public class TestCustomFieldParamsImpl extends ListeningTestCase
{
    Mock customFieldMock;
    CustomFieldParamsImpl params;

    @Before
    public void setUp()
    {
        customFieldMock = new Mock(CustomField.class);
        customFieldMock.expectAndReturn("getId", "customField_123");

        params = new CustomFieldParamsImpl((CustomField) customFieldMock.proxy());
    }

    @Test
    public void testGetDefaultParameterQueryString()
    {
        params.addValue(null, EasyList.build("Margaret"));
        assertEquals("customField_123=Margaret", params.getQueryString());
    }

    @Test
    public void testGetQueryStringIgnoresNulls()
    {
        params.addValue(null, null);
        assertEquals("", params.getQueryString());
    }

    @Test
    public void testGetSingleParameterQueryString()
    {
        params.addValue("name", EasyList.build("Bob"));
        assertEquals("customField_123:name=Bob", params.getQueryString());
    }

    @Test
    public void testGetMultiParameterQueryString()
    {
        params.addValue("name", EasyList.build("Angela", "Duncan"));
        assertEquals("customField_123:name=Angela&customField_123:name=Duncan", params.getQueryString());
    }

    @Test
    public void testGetMultiDifferentParameterQueryString()
    {
        params.addValue("name", EasyList.build("Angela", "Duncan"));
        params.addValue("age", EasyList.build("21"));
        params.addValue(null, EasyList.build("Bob"));
        String param1 = "customField_123:age=21";
        String param2 = "customField_123=Bob";
        String param3 = "customField_123:name=Angela";
        String param4 = "customField_123:name=Duncan";

        assertEquals((param1 + "&" + param2 + "&" + param3 + "&" + param4).length(), params.getQueryString().length());
        assertTrue(params.getQueryString().indexOf(param1) != -1);
        assertTrue(params.getQueryString().indexOf(param2) != -1);
        assertTrue(params.getQueryString().indexOf(param3) != -1);
        assertTrue(params.getQueryString().indexOf(param4) != -1);
    }


}
