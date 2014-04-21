package com.atlassian.jira.util.map;

import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.local.ListeningTestCase;

import java.util.Map;

/**
 * Test for {@link com.atlassian.jira.util.map.NotNullHashMap}.
 *
 * @since v4.1
 */
public class TestNotNullHashMap extends ListeningTestCase
{
    @Test
    public void testPut() throws Exception
    {
        final Map<String, String> map = new NotNullHashMap<String, String>();
        assertEquals(map.put("test", "value"), null);
        assertEquals(map.put("test", "value2"), "value");
        assertEquals(map.put("test", null), "value2");
    }
}
