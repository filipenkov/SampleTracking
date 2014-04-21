package com.atlassian.jira.util.json;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.core.util.collection.EasyList;

public class TestJSONArray extends ListeningTestCase
{
    @Test
    public void testIsNull() throws Exception
    {
        final JSONArray jsonArray = new JSONArray(EasyList.build("Text", JSONObject.NULL));

        assertTrue(jsonArray.isNull(-10)); // null 'cause index is out of bounds
        assertTrue(jsonArray.isNull(10));  // null 'cause index is out of bounds
        assertTrue(jsonArray.isNull(1));   // nusl 'cause it is NULL object

        assertFalse(jsonArray.isNull(0));
    }

    @Test
    public void testToStringWithException() throws Exception
    {
        final JSONArray jsonArray = new JSONArray()
        {
            public String join(final String separator) throws JSONException
            {
                throw new RuntimeException("intentional");
            }
        };

        assertEquals("", jsonArray.toString());
    }

    @Test
    public void testToString() throws Exception
    {
        final JSONArray jsonArray = new JSONArray(EasyList.build("Text", JSONObject.NULL));

        assertEquals("[\"Text\",null]", jsonArray.toString());
    }

}
