/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.type.RemoteUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.List;

public class TestRemoteUser extends AbstractUsersTestCase
{
    private User u1;

    public TestRemoteUser(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        u1 = createMockUser("Group Member 1", "", "owen@atlassian.com");
        new JiraUserPreferences(u1).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
    }

    public void testGetDisplayName()
    {
        RemoteUser gd = new RemoteUser(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("Current User", gd.getDisplayName());
    }

    public void testGetRecipients()
    {
        RemoteUser gd = new RemoteUser(ComponentAccessor.getJiraAuthenticationContext());
        List recipients = gd.getRecipients(new IssueEvent(null, u1, null, null, null, null, null), null);

        // List recipients = gd.getRecipients(null, u1, null, null);
        assertEquals(1, recipients.size());

        NotificationRecipient nr = (NotificationRecipient) recipients.get(0);
        assertEquals("owen@atlassian.com", nr.getEmail());
        assertTrue(nr.isHtml());
    }
}
