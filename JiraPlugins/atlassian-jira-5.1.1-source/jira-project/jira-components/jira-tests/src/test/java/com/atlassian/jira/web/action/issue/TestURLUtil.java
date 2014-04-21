/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

public class TestURLUtil extends ListeningTestCase
{
    @Test
    public void testParameterAddedToRawURL()
    {
        final String rawUrl = "http://example.com";
        String result = URLUtil.addRequestParameter(rawUrl, "temp=100");
        assertNotNull(result);
        assertEquals("http://example.com?temp=100", result);
    }

    @Test
    public void testParameterAddedToURLWithParameters()
    {
        final String rawUrl = "http://example.com?something=200";
        String result = URLUtil.addRequestParameter(rawUrl, "temp=100");
        assertNotNull(result);
        assertEquals("http://example.com?something=200&temp=100", result);
    }

    @Test
    public void testNullParameterAddedToRawURL()
    {
        final String rawUrl = "http://example.com";
        String result = URLUtil.addRequestParameter(rawUrl, null);
        assertNotNull(result);
        assertEquals("http://example.com", result);
    }

    @Test
    public void testNullURLThrowsNPE()
    {
        try
        {
            URLUtil.addRequestParameter(null, null);
            fail("Should have thrown NPE");
        }
        catch (NullPointerException yay)
        {
            // expected
        }
    }
}
