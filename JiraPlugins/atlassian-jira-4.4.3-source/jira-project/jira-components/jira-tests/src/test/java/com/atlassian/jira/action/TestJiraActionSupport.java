/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.opensymphony.user.User;
import mock.action.MockAction;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import webwork.action.Action;
import webwork.dispatcher.ActionResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestJiraActionSupport extends AbstractWebworkTestCase
{

    private static final class MockJiraActionSupport extends JiraActionSupport {
        @Override
        public User getLoggedInUser()
        {
            return null;
        }

        @Override
        public User getRemoteUser()
        {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    public TestJiraActionSupport(String s)
    {
        super(s);
    }

    public void testGetActionName()
    {
        JiraActionSupport jas = new JiraNonWebActionSupport();

        assertEquals("JiraNonWebActionSupport", jas.getActionName());
    }

    public void testGetResult()
    {
        JiraActionSupport jas = new JiraNonWebActionSupport();
        assertEquals(Action.SUCCESS, jas.getResult());

        jas = new JiraNonWebActionSupport();
        jas.addErrorMessage("An error has occurred");
        assertEquals(Action.ERROR, jas.getResult());

        jas = new JiraNonWebActionSupport();
        jas.addError("foo", "An error has occurred");
        assertEquals(Action.ERROR, jas.getResult());
    }

    public void testAddErrorMessages()
    {
        JiraActionSupport jas = new JiraNonWebActionSupport();

        assertEquals(0, jas.getErrorMessages().size());

        List newErrorMessages = new ArrayList();

        newErrorMessages.add("foo");
        newErrorMessages.add("bar");

        jas.addErrorMessages(newErrorMessages);

        assertEquals(newErrorMessages, jas.getErrorMessages());
    }

    public void testAddErrors()
    {
        JiraActionSupport jas = new JiraNonWebActionSupport();
        assertEquals(0, jas.getErrors().size());

        Map errors = new HashMap();
        errors.put("foo", "bar");
        errors.put("baz", "bat");
        jas.addErrors(errors);
        assertEquals(2, jas.getErrors().size());
        assertEquals("bar", jas.getErrors().get("foo"));
        assertEquals("bat", jas.getErrors().get("baz"));
    }

    public void testGetDelegator() throws Exception
    {
        JiraActionSupport jas = new JiraNonWebActionSupport();
        assertNotNull(jas.getDelegator());
        assertEquals("default", jas.getDelegator().getDelegatorName());
    }

    public void testAddErrorMessagesFromAResult()
    {
        ActionResult result = new ActionResult(Action.SUCCESS, null, null, null);

        JiraActionSupport jas = new JiraNonWebActionSupport();
        jas.addErrorMessages(result);

        assertEquals(0, jas.getErrorMessages().size());

        // now create a dummy action with an error message and check it gets added
        MockAction ma = new MockAction();
        ma.addErrorMessage("an error message");
        result = new ActionResult(Action.ERROR, null, EasyList.build(ma), null);
        jas.addErrorMessages(result);

        assertEquals(1, jas.getErrorMessages().size());
        assertEquals("an error message", jas.getErrorMessages().iterator().next());
    }

    public void testRemoveKeyOrAddError()
    {
        JiraActionSupport jas = new MockJiraActionSupport();
        Map params = new HashMap();
        assertTrue(params.isEmpty());
        assertTrue(jas.getErrorMessages().isEmpty());

        jas.removeKeyOrAddError(params, "absentparam", "the.error");
        assertTrue(params.isEmpty());
        assertEquals(1, jas.getErrorMessages().size());
        assertTrue(jas.getErrorMessages().contains("the.error"));

        jas.removeKeyOrAddError(params, "absentparam", "the.error");

        Map existing = EasyMap.build("count", "5");
        jas = new JiraNonWebActionSupport();

        assertTrue(existing.containsKey("count"));
        jas.removeKeyOrAddError(existing, "count", "admin.errors.workflows.cannot.find.count.bogus");
        assertTrue(existing.isEmpty());
        assertEquals(0, jas.getErrorMessages().size());

        assertFalse(existing.containsKey("count"));
        jas.removeKeyOrAddError(existing, "count", "admin.errors.workflows.cannot.find.count.bogus");
        assertTrue(existing.isEmpty());
        assertEquals(1, jas.getErrorMessages().size());
        assertTrue(jas.getErrorMessages().toString(), jas.getErrorMessages().contains("admin.errors.workflows.cannot.find.count.bogus"));
    }
}
