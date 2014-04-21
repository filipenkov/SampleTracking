/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.vcs.viewcvs;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

import java.net.MalformedURLException;
import java.util.Collections;

public class TestViewCvsBrowser extends LegacyJiraMockTestCase
{
    String badBaseURL = "badBaseURL";
    String baseURL = "http://baseURL/";
    String type = "VIEW_CVS";
    String filePath = "filePath";

    public void testGetters() throws MalformedURLException
    {
        // Create browser with no root parameter
        ViewCvsBrowser viewCvsBrowser = new ViewCvsBrowser(baseURL, Collections.EMPTY_MAP);

        assertEquals(baseURL, viewCvsBrowser.getBaseURL());

        assertEquals(type, viewCvsBrowser.getType());

        assertEquals(baseURL + filePath, viewCvsBrowser.getFileLink(filePath));

        assertEquals(baseURL + filePath + "?rev=9", viewCvsBrowser.getRevisionLink(filePath, "9"));

        assertEquals(baseURL + filePath + "?r1=1.1&r2=1.2", viewCvsBrowser.getDiffLink(filePath, "1.2"));
        assertEquals(baseURL + filePath + "?r1=1.9&r2=1.10", viewCvsBrowser.getDiffLink(filePath, "1.10"));
        assertEquals(baseURL + filePath + "?r1=1.10&r2=1.11", viewCvsBrowser.getDiffLink(filePath, "1.11"));
        assertEquals(baseURL + filePath + "?r1=1.2&r2=1.2.2.1", viewCvsBrowser.getDiffLink(filePath, "1.2.2.1"));
        assertEquals(baseURL + filePath + "?r1=1.2.2.1&r2=1.2.2.1.2.1", viewCvsBrowser.getDiffLink(filePath, "1.2.2.1.2.1"));
    }

    public void testGettersWithRoot() throws MalformedURLException
    {
        final String rootParam = "mycvsroot";

        ViewCvsBrowser viewCvsBrowser = new ViewCvsBrowser(baseURL, EasyMap.build(ViewCvsBrowser.ROOT_PARAMETER, rootParam));

        assertEquals(baseURL, viewCvsBrowser.getBaseURL());

        assertEquals(type, viewCvsBrowser.getType());

        ApplicationProperties ap = ManagerFactory.getApplicationProperties();
        ap.setString(APKeys.VIEWCVS_ROOT_TYPE, "cvsroot");
        assertEquals(baseURL + filePath + "?cvsroot=" + rootParam + "&r1=1.9&r2=1.10", viewCvsBrowser.getDiffLink(filePath, "1.10"));

        ap.setString(APKeys.VIEWCVS_ROOT_TYPE, "root");
        assertEquals(baseURL + filePath + "?root=" + rootParam + "&r1=1.9&r2=1.10", viewCvsBrowser.getDiffLink(filePath, "1.10"));
    }

    public void testConstructorWithBadBaseURL()
    {
        try
        {
            new ViewCvsBrowser(badBaseURL, Collections.EMPTY_MAP);
        }
        catch (MalformedURLException mue)
        {
            assertEquals(mue.getLocalizedMessage(), "Invalid URL '" + badBaseURL + "'.");
        }
    }
}

