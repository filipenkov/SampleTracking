/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.action.admin;

import com.atlassian.jira.issue.index.IndexException;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.local.ListeningTestCase;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.action.admin.IndexOptimize;
import com.mockobjects.dynamic.Mock;
import org.junit.Before;
import org.junit.Test;
import webwork.action.Action;

import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TestIndexOptimize extends ListeningTestCase
{
    private Mock mockIssueIndexManager = new Mock(IssueIndexManager.class);
    private IndexOptimize action;

    @Before
    public void setUp()
    {
        mockIssueIndexManager.setStrict(true);
        final MockI18nHelper i18nHelper = new MockI18nHelper();
        action = new IndexOptimize((IssueIndexManager) mockIssueIndexManager.proxy())
        {
            public String getRedirect(final String defaultUrl)
            {
                return defaultUrl;
            }

            @Override
            protected <T> T getComponentInstanceOfType(Class<T> clazz)
            {
                return null;
            }

            @Override
            protected I18nHelper getI18nHelper()
            {
                return i18nHelper;
            }
        };
    }

    @Test
    public void testNullInCtorDoesntWork()
    {
        try
        {
            IndexOptimize indexOptimize = new IndexOptimize(null) {
                @Override
                protected <T> T getComponentInstanceOfType(Class<T> clazz)
                {
                    return null;
                }
            };
            fail("NPE expected");
        }
        catch (NullPointerException yay)
        {
        }
    }

    @Test
    public void testGettersAndSetters()
    {
        assertEquals(0, action.getOptimizeTime());
        action.setOptimizeTime(123);
        assertEquals(123, action.getOptimizeTime());
        action.setOptimizeTime(123098765);
        assertEquals(123098765, action.getOptimizeTime());
        mockIssueIndexManager.verify();
    }

    @Test
    public void testValidationFailureIfIndexingDisabled() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.FALSE);

        assertFalse(action.isIndexing());
        assertEquals(Action.INPUT, action.execute());
        Collection errors = action.getErrorMessages();
        assertEquals(1, errors.size());
        assertEquals("admin.indexing.optimize.index.disabled", ((String) errors.iterator().next()));
        mockIssueIndexManager.verify();
    }

    @Test
    public void testErrorIfIndexingLockFails() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        mockIssueIndexManager.expectAndReturn("optimize", new Long(-1));

        assertTrue(action.isIndexing());
        assertEquals(Action.ERROR, action.execute());
        Collection errors = action.getErrorMessages();
        assertEquals(1, errors.size());
        assertEquals("admin.indexing.optimize.index.nolock", (String) errors.iterator().next());
        mockIssueIndexManager.verify();
    }

    @Test
    public void testErrorIfIndexingThrowsUp() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        mockIssueIndexManager.expectAndThrow("optimize", new IndexException("You're screwed..."));

        assertTrue(action.isIndexing());
        assertEquals(Action.ERROR, action.execute());
        Collection errors = action.getErrorMessages();
        assertEquals(1, errors.size());
        assertEquals("admin.indexing.optimizing.error", (String) errors.iterator().next());
        mockIssueIndexManager.verify();
    }

    @Test
    public void testCorrectResult() throws Exception
    {
        mockIssueIndexManager.expectAndReturn("isIndexingEnabled", Boolean.TRUE);
        mockIssueIndexManager.expectAndReturn("optimize", new Long(1234));

        assertTrue(action.isIndexing());
        assertEquals("IndexOptimize!default.jspa?optimizeTime=1234", action.execute());
        Collection errors = action.getErrorMessages();
        assertEquals(0, errors.size());
        mockIssueIndexManager.verify();
    }
}
