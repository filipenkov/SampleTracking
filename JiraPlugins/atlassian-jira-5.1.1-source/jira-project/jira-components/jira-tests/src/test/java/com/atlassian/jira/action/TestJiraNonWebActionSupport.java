/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.user.MockUser;
import webwork.action.Action;
import webwork.action.ActionContext;

public class TestJiraNonWebActionSupport extends AbstractUsersTestCase
{
    private static final String FRED = "TestJiraNonWebActionSupport_fred";

    public TestJiraNonWebActionSupport(String s)
    {
        super(s);
    }

    public void testNoDoDefault() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        try
        {
            as.doDefault();
            fail("An exception should be thrown.");
        }
        catch (UnsupportedOperationException e)
        {
            // Do nothing
        }
    }

    public void testDispatchEvent()
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        assertTrue(as.isDispatchEvent());

        as.setDispatchEvent(false);
        assertTrue(!as.isDispatchEvent());
    }

    public void testRemoteUserViaSetter() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        User user = new MockUser("fred");
        as.setRemoteUser(user);

        assertEquals(user, as.getLoggedInUser());
    }

    public void testRemoteUserViaContext() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        User user = createMockUser("fred");
        ActionContext.setPrincipal(user);

        assertEquals(user, as.getLoggedInUser());
    }

    public void testRemoteUserNullViaContext() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        ActionContext.setPrincipal(null);

        assertNull(as.getLoggedInUser());
    }

    public void testDoExecuteInput() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();

        as.addErrorMessage("an error message to make it return input");

        assertEquals(Action.ERROR, as.execute());
    }

    public void testDoExecute() throws Exception
    {
        JiraNonWebActionSupport as = new JiraNonWebActionSupport();
        assertEquals(Action.SUCCESS, as.execute());
    }
}
