/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationType;
import com.atlassian.jira.notification.NotificationTypeManager;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.List;

public class TestNotificationTypeManager extends AbstractUsersTestCase
{
    private static final String EMAIL_ADDRESS = "owen@atlassian.com";

    public TestNotificationTypeManager(String s)
    {
        super(s);
    }

    public void testGetNotificationTypeFail()
    {
        NotificationTypeManager ntm = new NotificationTypeManager("test-notification-event-types.xml");
        assertNull(ntm.getNotificationType(""));
    }

    public void testGetNotificationType()
    {
        NotificationTypeManager ntm = new NotificationTypeManager("test-notification-event-types.xml");
        NotificationType nt = ntm.getNotificationType("TEST_TYPE_1");
        assertNotNull(nt);

        IssueEvent event = new IssueEvent(null, null, null, null);
        List addresses = nt.getRecipients(event, EMAIL_ADDRESS);

        //        List addresses = nt.getRecipients(null, null, EMAIL_ADDRESS, null);
        assertEquals(1, addresses.size());
    }
}
