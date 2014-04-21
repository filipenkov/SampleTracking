/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.JiraTestUtil;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.web.action.admin.notification.DeleteScheme;
import com.mockobjects.servlet.MockHttpServletResponse;
import org.ofbiz.core.entity.GenericValue;
import webwork.action.Action;

public class TestDeleteScheme extends AbstractWebworkTestCase
{
    public TestDeleteScheme(String s)
    {
        super(s);
    }

    public void testGetsSets()
    {
        DeleteScheme ds = new DeleteScheme();
        assertNull(ds.getSchemeId());
        assertTrue(!ds.isConfirmed());
        ds.setSchemeId(new Long(1));
        ds.setConfirmed(true);
        assertEquals(new Long(1), ds.getSchemeId());
        assertTrue(ds.isConfirmed());
    }

    public void testValidation() throws Exception
    {
        DeleteScheme ds = new DeleteScheme();
        String result = ds.execute();
        assertEquals(Action.INPUT, result);
        assertEquals(1, ds.getErrors().size());
        assertEquals("Please specify a Scheme to delete.", ds.getErrors().get("schemeId"));
        assertEquals(1, ds.getErrorMessages().size());
        assertTrue(ds.getErrorMessages().contains("Please confirm you wish to delete this Scheme."));
    }

    public void testExecute() throws Exception
    {
        MockHttpServletResponse response = JiraTestUtil.setupExpectedRedirect("ViewNotificationSchemes.jspa");

        GenericValue scheme = com.atlassian.core.ofbiz.test.UtilsForTests.getTestEntity("NotificationScheme", EasyMap.build("name", "This Name"));
        DeleteScheme ds = new DeleteScheme();
        ds.setSchemeId(scheme.getLong("id"));
        ds.setConfirmed(true);

        String result = ds.execute();
        assertEquals(Action.NONE, result);
        assertTrue(!ManagerFactory.getNotificationSchemeManager().schemeExists("This Name"));

        response.verify();
    }
}
