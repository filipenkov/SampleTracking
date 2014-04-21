package com.atlassian.trackback;

import junit.framework.TestCase;

import java.util.Map;

/**
 * Test {@link DefaultTrackbackSender}
 */
public class TestDefaultTrackbackSender extends TestCase
{
    public void testHttpRequestHeaders() throws Exception
    {
        final DefaultTrackbackSender sender  = new DefaultTrackbackSender();
        Map headerMap = sender.buildHttpHeaders("/trackback/someurl/somehere", null);
        assertNotNull(headerMap);
        assertEquals(1,headerMap.size());
        assertTrue(headerMap.containsKey("Content-Type"));

        Object value = headerMap.get("Content-Type");
        assertTrue(value instanceof String);

        assertEquals("application/x-www-form-urlencoded; charset=utf-8", value.toString());
    }
}
