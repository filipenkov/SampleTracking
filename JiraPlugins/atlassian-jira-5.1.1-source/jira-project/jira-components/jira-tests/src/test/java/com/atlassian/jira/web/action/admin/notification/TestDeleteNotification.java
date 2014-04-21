/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.notification.DeleteNotification;
import com.mockobjects.servlet.MockHttpServletResponse;
import webwork.action.Action;

import java.util.List;

public class TestDeleteNotification extends AbstractWebworkTestCase
{
    public TestDeleteNotification(String s)
    {
        super(s);
    }

    public void testGetsSets()
    {
        DeleteNotification dn = new DeleteNotification();
        assertNull(dn.getId());
        dn.setId(new Long(1));
        assertEquals(new Long(1), dn.getId());
        assertTrue(!dn.isConfirmed());
        dn.setConfirmed(true);
        assertTrue(dn.isConfirmed());
    }

    public void testValidation() throws Exception
    {
        DeleteNotification dn = new DeleteNotification();
        String result = dn.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(2, dn.getErrorMessages().size());
        assertTrue(dn.getErrorMessages().contains("Please confirm you which to delete this Notification."));
        assertTrue(dn.getErrorMessages().contains("Please specify a Notification to delete."));
    }

    public void testExecute1() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        UtilsForTests.getTestEntity("Notification", EasyMap.build("id", new Long(1)));

        DeleteNotification dn = new DeleteNotification();
        dn.setId(new Long(1));
        dn.setConfirmed(true);

        String result = dn.execute();
        assertEquals(Action.NONE, result);

        List notifications = CoreFactory.getGenericDelegator().findAll("Notification");
        assertTrue(notifications.isEmpty());

        response.verify();
    }

    public void testExecute2() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("EditNotifications!default.jspa?schemeId=1000");

        UtilsForTests.getTestEntity("Notification", EasyMap.build("id", new Long(1)));

        DeleteNotification dn = new DeleteNotification();
        dn.setId(new Long(1));
        dn.setConfirmed(true);
        dn.setSchemeId(new Long(1000));

        String result = dn.execute();
        assertEquals(Action.NONE, result);

        List notifications = CoreFactory.getGenericDelegator().findAll("Notification");
        assertTrue(notifications.isEmpty());

        response.verify();
    }
}
