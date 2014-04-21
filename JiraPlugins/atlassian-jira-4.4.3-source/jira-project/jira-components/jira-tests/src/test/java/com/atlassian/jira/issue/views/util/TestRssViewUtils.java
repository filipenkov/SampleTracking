package com.atlassian.jira.issue.views.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.Locale;

public class TestRssViewUtils extends ListeningTestCase
{
    @Test
    public void testRssViewLocale()
    {
        assertNull(RssViewUtils.getRssLocale(null));
        assertEquals("fr-ca", RssViewUtils.getRssLocale(Locale.CANADA_FRENCH));
        assertEquals("fr-fr", RssViewUtils.getRssLocale(Locale.FRANCE));
        assertEquals("ko", RssViewUtils.getRssLocale(Locale.KOREAN));
    }
}
