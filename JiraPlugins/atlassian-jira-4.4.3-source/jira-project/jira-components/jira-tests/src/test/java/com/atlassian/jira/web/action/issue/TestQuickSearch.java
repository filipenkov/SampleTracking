/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import org.junit.After;
import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.*;
import static com.atlassian.jira.util.EasyList.build;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.util.TestJiraKeyUtils;
import com.atlassian.jira.util.JiraKeyUtils.KeyMatcher;

import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import com.mockobjects.servlet.MockHttpServletResponse;

import java.util.Collection;
import java.util.Collections;

public class TestQuickSearch extends ListeningTestCase
{
    private GenericValue testProject;
    private MockHttpServletResponse response;
    private KeyMatcher oldKeyMatcher;

    @Before
    public void setUp() throws Exception
    {
        testProject = new MockGenericValue("Project", EasyMap.build("name", "ProjectA", "key", "JRA", "lead", "dave"));
        response = JiraTestUtil.setupExpectedRedirect("/browse/JRA-100");
        oldKeyMatcher = TestJiraKeyUtils.getCurrentKeyMatcher();
        TestJiraKeyUtils.setKeyMatcher(new TestJiraKeyUtils.MockKeyMatcher(""));
    }

    @After
    public void tearDown() throws Exception
    {
        testProject = null;
        response = null;
        TestJiraKeyUtils.setKeyMatcher(oldKeyMatcher);
    }

    @Test
    public void testGetNumberOnlyWithSelectedProject() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null)
        {
            @Override
            public GenericValue getSelectedProject()
            {
                return testProject;
            }

            @Override
            public boolean isHasPermission(final String permName)
            {
                return true;
            }
        };
        qs.setSearchString("100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testOnlyOneBrowsableProject() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null)
        {
            @Override
            public Collection<GenericValue> getBrowseableProjects()
            {
                return build(testProject);
            }

            @Override
            public GenericValue getSelectedProject()
            {
                return null;
            }
        };

        qs.setSearchString("100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testValidKey() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null);
        qs.setSearchString("JRA-100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testForNonCaseSensitivity() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null);
        qs.setSearchString("jRa-100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testInvalidKey() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null)
        {
            @Override
            public String getKey()
            {
                return null;
            }

            @Override
            protected String createQuery(final String query)
            {
                assertEquals("sdafsadf", query);
                return "testredirect";
            }

            @Override
            protected void sendInternalRedirect(final String searchString)
            {
                assertEquals("testredirect", searchString);
            }
        };
        qs.setSearchString("sdafsadf");

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testValidKeyForANonExistantIssue() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null);
        qs.setSearchString("JRA-100");

        final String result = qs.execute();
        assertEquals(Action.NONE, result);

        response.verify();
    }

    @Test
    public void testUserEntersANumberOnlyInQuickSearch() throws Exception
    {
        response = JiraTestUtil.setupExpectedRedirect("/browse/10000");

        final QuickSearch qs = new QuickSearch(null, null, null)
        {
            @Override
            public GenericValue getSelectedProject()
            {
                return null;
            }

            @Override
            public Collection<GenericValue> getBrowseableProjects()
            {
                return Collections.emptyList();
            }
        };

        qs.setSearchString("10000");
        assertEquals("10000", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);

        response.verify();
    }

    /**
     * Test that verifies JRA-2233
     */
    @Test
    public void testQuickSearchReturnsOriginalKey() throws Exception
    {
        response = JiraTestUtil.setupExpectedRedirect("testredirect");

        final String ORIG_SEARCH_STRING = "my search string";
        final QuickSearch qs = new QuickSearch(null, null, null)
        {
            @Override
            protected String createQuery(final String searchString)
            {
                assertEquals(ORIG_SEARCH_STRING, searchString);
                return "testredirect";
            }

            @Override
            protected void sendInternalRedirect(final String searchString)
            {
                assertEquals("testredirect", searchString);
            }
        };
        qs.setSearchString(ORIG_SEARCH_STRING);
        qs.execute();
    }
}
