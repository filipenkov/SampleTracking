package com.atlassian.jira.webtest.framework.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for JQueryUtils.
 *
 * @since v4.3
 */
public class JQueryUtilsTest
{
    @Test
    public void testEscapeJQuery() throws Exception
    {
        String metaChars = "!\"#$%&'()*+,./:;?@[\\]^`{|}~";
        for (int i = 0; i < metaChars.length(); i++)
        {
            char c = metaChars.charAt(i);
            assertEquals("Character needs to be escaped with '\\\\' prefix", "\\\\" + c, JQueryUtils.escapeJQuery(String.valueOf(c)));
        }
    }
}
