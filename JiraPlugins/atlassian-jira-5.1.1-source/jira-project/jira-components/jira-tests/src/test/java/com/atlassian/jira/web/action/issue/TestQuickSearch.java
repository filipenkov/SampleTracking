/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.MockEventPublisher;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.JiraKeyUtils.KeyMatcher;
import com.atlassian.jira.util.TestJiraKeyUtils;
import com.google.common.collect.Lists;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import webwork.action.Action;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class TestQuickSearch extends ListeningTestCase
{
    private Project testProject;
    private MockHttpServletResponse response;
    private KeyMatcher oldKeyMatcher;

    @Before
    public void setUp() throws Exception
    {
        testProject = new MockProject(new MockGenericValue("Project", EasyMap.build("name", "ProjectA", "key", "JRA", "lead", "dave")));
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
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher())
        {
            @Override
            public Project getSelectedProjectObject()
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
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher())
        {
            @Override
            public Collection<Project> getBrowsableProjects()
            {
                return Lists.newArrayList(testProject);
            }

            @Override
            public Project getSelectedProjectObject()
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
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher());
        qs.setSearchString("JRA-100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testForNonCaseSensitivity() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher());
        qs.setSearchString("jRa-100");
        assertEquals("JRA-100", qs.getKey());

        final String result = qs.execute();
        assertEquals(Action.NONE, result);
    }

    @Test
    public void testInvalidKey() throws Exception
    {
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher())
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
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher());
        qs.setSearchString("JRA-100");

        final String result = qs.execute();
        assertEquals(Action.NONE, result);

        response.verify();
    }

    @Test
    public void testUserEntersANumberOnlyInQuickSearch() throws Exception
    {
        response = JiraTestUtil.setupExpectedRedirect("/browse/10000");

        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher())
        {
            @Override
            public Project getSelectedProjectObject()
            {
                return null;
            }

            @Override
            public Collection<Project> getBrowsableProjects()
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
        final QuickSearch qs = new QuickSearch(null, null, null, new MockEventPublisher())
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
