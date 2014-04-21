/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.trackback;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.Test;
import static org.junit.Assert.*;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.core.util.collection.EasyList;

import java.util.Collection;
import java.util.List;

public class TestPingUrlFilterer extends ListeningTestCase
{
    @Test
    public void testBaseURL()
    {
        _testMatches("", "http://jira.server.com/browse/ABC", false);
        _testMatches("http://jira.server.com/", "http://jira.server.com/browse/ABC", true);
        _testMatches("http://jira.server.com/", "http://jira.server.edu/browse/ABC", false);
    }

    public void _testMatches(String baseUrl, String pingUrl, boolean isLimited)
    {
        _testMatches(baseUrl, "", EasyList.build(pingUrl), isLimited ? null : EasyList.build(pingUrl));
    }

    public void _testMatches(String baseUrl, String trackbackExcludes, List pingUrls, Collection allowedUrls)
    {
        ApplicationPropertiesImpl applicationProperties = new MyApplicationProperties(baseUrl, trackbackExcludes);
        PingUrlFilterer pingUrlFilterer = new PingUrlFilterer(applicationProperties);
        if (allowedUrls == null || allowedUrls.isEmpty())
            assertTrue(pingUrlFilterer.filterPingUrls(pingUrls).isEmpty());
        else
            assertTrue(pingUrlFilterer.filterPingUrls(pingUrls).containsAll(allowedUrls));
    }


    @Test
    public void testPatternIsExcluded()
    {
        _testMatches("", "http://dfenz.com/.*", EasyList.build("http://dfenz.com/browse/ABC"), null); //this url is filtered
        _testMatches("", "http://serverA.com", EasyList.build("http://anotherServer.com/browse/ABC"), EasyList.build("http://anotherServer.com/browse/ABC"));
        _testMatches("", "http://serverA.com", EasyList.build("http://anotherServer.com/browse/ABC"), EasyList.build("http://anotherServer.com/browse/ABC"));
        _testMatches("baseUrl", "http://serverA.com", EasyList.build("http://anotherServer.com/browse/ABC"), EasyList.build("http://anotherServer.com/browse/ABC"));
    }

    @Test
    public void testPatternWithNewline()
    {
        List pingUrls = EasyList.build("http://jira.server.com/browse/ABC", "http://dfenz.com/browse/ABC", "http://ofenz.edu/browse/ABC");
        _testMatches("http://jira.server.com", "http://dfenz.com.*\nhttp://ofenz.edu.*", pingUrls, null); // they are all filtered

        pingUrls = EasyList.build("http://jira.server.com/browse/ABC", "http://dfenz.com/browse/ABC", "http://ofenz.edu/browse/ABC");
        _testMatches("http://baseUrl.com", "http://anotherServer.com.*\nhttp://differentServerAgain.edu.*", pingUrls, pingUrls); // they are not filtered
    }


    @Test
    public void testIPIsExcluded()
    {
        List pingUrls = EasyList.build("http://101.1.1.101/browse/ABC");
        _testMatches("", ".*101\\.1\\.1\\.101.*", pingUrls, null); // they are all filtered

    }

    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private final String baseUrl;
        private final String trackbackExcludes;

        public MyApplicationProperties(String baseUrl, String trackbackExcludes)
        {
            super(null);
            this.baseUrl = baseUrl;
            this.trackbackExcludes = trackbackExcludes;
        }

        public String getDefaultBackedString(String name)
        {
            if (APKeys.JIRA_BASEURL.equals(name))
                return baseUrl;
            else if (APKeys.JIRA_TRACKBACK_EXCLUDE_PATTERN.equals(name))
                return trackbackExcludes;
            else
                return null;
        }
    }
}
