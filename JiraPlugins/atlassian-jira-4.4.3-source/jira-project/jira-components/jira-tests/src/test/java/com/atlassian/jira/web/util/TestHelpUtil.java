/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.util;

import com.atlassian.jira.local.ListeningTestCase;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import com.atlassian.core.util.ClassLoaderUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class TestHelpUtil extends ListeningTestCase
{
    private static final Logger log = Logger.getLogger(TestHelpUtil.class);

    private final String SUFFIX = "?suffix";
    private final String PREFIX = "prefix++";
    private final String DEFAULT_ALT = "DEFAULT_ALT";
    private final String DEFAULT_TITLE = "DEFAULT_TITLE";
    private final String DEFAULT_URL = "DEFAULT_URL";
    Properties props;

    @Test
    public void testGetValue()
    {
        props.setProperty("permissions.url", "PERMISSIONS_URL");
        props.setProperty("permissions.alt", "PERMISSIONS_ALT");
        props.setProperty("permissions.title", "PERMISSIONS_TITLE");

        HelpUtil helpUtil = new HelpUtil(props);

        HelpUtil.HelpPath helpPath = helpUtil.getHelpPath("permissions");
        assertNotNull(helpPath);
        assertEquals(PREFIX + "PERMISSIONS_URL" + SUFFIX, helpPath.getUrl());
        assertEquals("PERMISSIONS_ALT", helpPath.getAlt());
        assertEquals("PERMISSIONS_TITLE", helpPath.getTitle());
    }

    @Test
    public void testDefaultValues()
    {
        props.setProperty("permissions.url", "PERMISSIONS_URL");

        HelpUtil helpUtil = new HelpUtil(props);

        HelpUtil.HelpPath helpPath = helpUtil.getHelpPath("permissions");
        assertNotNull(helpPath);
        assertEquals(PREFIX + "PERMISSIONS_URL" + SUFFIX, helpPath.getUrl());
        assertEquals(DEFAULT_ALT, helpPath.getAlt());
        assertEquals(DEFAULT_TITLE, helpPath.getTitle());
    }

    @Test
    public void testAnchorInTheUrl()
    {
        props.setProperty("anchor.url", "url_with#an_anchor_in_it");

        HelpUtil helpUtil = new HelpUtil(props);

        HelpUtil.HelpPath helpPath = helpUtil.getHelpPath("anchor");
        assertNotNull(helpPath);
        assertEquals(PREFIX + "url_with" + SUFFIX + "#an_anchor_in_it", helpPath.getUrl());
        assertEquals(DEFAULT_ALT, helpPath.getAlt());
        assertEquals(DEFAULT_TITLE, helpPath.getTitle());
    }

    @Test
    public void testKeySet()
    {
        props.setProperty("keyname.url", "a value");

        HelpUtil helpUtil = new HelpUtil(props);
        Set<String> keySet = helpUtil.keySet();
        assertNotNull(keySet);
        assertEquals(2,keySet.size());
        assertTrue(keySet.contains("default"));
        assertTrue(keySet.contains("keyname"));
    }

    @Before
    public void setUp()
    {
        props = new Properties();
        props.setProperty("url-suffix", SUFFIX);
        props.setProperty("url-prefix", PREFIX);

        props.setProperty("default.url", DEFAULT_URL);
        props.setProperty("default.alt", DEFAULT_ALT);
        props.setProperty("default.title", DEFAULT_TITLE);
    }

    //this should restore the static stuff in HelpUtil back to something normal.
    @After
    public void tearDown()
    {
        new HelpUtil(loadProperties(HelpUtil.HELP_PATH_CONFIG_LOCATION));
    }

    private Properties loadProperties(String propertiesFileLocation)
    {
        Properties properties = new Properties();
        try
        {
            final InputStream is = ClassLoaderUtils.getResourceAsStream(propertiesFileLocation, HelpUtil.class);
            properties.load(is);
            is.close();
        }
        catch (IOException e)
        {
            log.error("Error loading helpfile " + propertiesFileLocation + ": " + e, e);
        }
        return properties;
    }
}
