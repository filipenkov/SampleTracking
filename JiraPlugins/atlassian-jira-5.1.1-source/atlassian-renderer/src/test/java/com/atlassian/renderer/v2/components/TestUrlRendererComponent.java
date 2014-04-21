package com.atlassian.renderer.v2.components;

import junit.framework.TestCase;

import java.util.regex.Matcher;

public class TestUrlRendererComponent extends TestCase
{
    /**
     * Tests for stack overflow caused by JDK regex bug (raised as CONF-9392).
     * <p/>
     * This test uses an extremely long URL, but it occurs with shorter URLs in Confluence because of Confluence's stack usage.
     */
    public void testStackDoesNotOverflowOnLargeUrls()
    {
        StringBuffer url = new StringBuffer("http://confluence.example.com/path/to/foo.action?param=value&");
        for (int i=0; i<2000; i++) // causes stack overflow with default stack allocation in a 64 MB JVM
            url.append("param").append(i).append("=I%20like+cheese,long;secret=7&").append("test=").append(257 * i).append("&");
        Matcher matcher = UrlRendererComponent.URL_PATTERN.matcher(url);
        if (!matcher.matches())
        {
            fail("Should match URL pattern");
        }
    }
}
