/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.admin.notification;

import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.local.AbstractWebworkTestCase;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.scheme.SchemeEntity;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class TestEditScheme extends AbstractWebworkTestCase
{
    private final Long TEST_EVENT_TYPE_ID = new Long(1);
    private GenericValue scheme;
    private GenericValue notification1;

    public TestEditScheme(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        NotificationSchemeManager nsm = ManagerFactory.getNotificationSchemeManager();
        scheme = nsm.createScheme("Test Scheme", null);
        nsm.createSchemeEntity(scheme, new SchemeEntity("TEST_TYPE_1", null, TEST_EVENT_TYPE_ID));
        notification1 = nsm.createSchemeEntity(scheme, new SchemeEntity("group", "TEST_TYPE_1", TEST_EVENT_TYPE_ID));
    }

    public void testGetEvents()
    {
        EditNotifications es = new EditNotifications(null);
        Map events = es.getEvents();
        assertNotNull(events);
    }

    public void testGetNotifications() throws GenericEntityException
    {
        EditNotifications es = new EditNotifications(null);
        es.setSchemeId(scheme.getLong("id"));

        List notifications = es.getNotifications(TEST_EVENT_TYPE_ID);
        assertTrue(!notifications.isEmpty());
        assertTrue(notifications.contains(notification1));
    }

    public void testGetNotification()
    {
        EditNotifications es = new EditNotifications(null);
        es.setSchemeId(scheme.getLong("id"));

        NotificationType type = es.getType("Current_Assignee");
        assertNotNull(type);
        assertEquals("Current Assignee", type.getDisplayName());
    }
}
