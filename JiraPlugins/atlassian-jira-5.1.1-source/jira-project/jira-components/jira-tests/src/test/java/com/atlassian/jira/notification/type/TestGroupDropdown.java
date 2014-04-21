/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.local.AbstractUsersTestCase;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TestGroupDropdown extends AbstractUsersTestCase
{
    private Group g1;
    private Group g2;
    private User u1;
    private User u2;
    private User u3;

    public TestGroupDropdown(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();
        g1 = createMockGroup("Group 1");
        g2 = createMockGroup("Group 2");
        u1 = createMockUser("Group Member 1", "Group Member 1", "owen@atlassian.com");
        new JiraUserPreferences(u1).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
        addUserToGroup(u1, g1);

        u2 = createMockUser("Group Member 2", "Group Member 2", "mike@atlassian.com");
        new JiraUserPreferences(u2).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
        addUserToGroup(u2, g1);

        u3 = createMockUser("Group Member 3", "Group Member 3", "scott@atlassian.com");
        new JiraUserPreferences(u3).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_TEXT);
        addUserToGroup(u3, g1);

        User u4 = createMockUser("Not Group Member", "Not Group Member", "noone@atlassian.com");
        new JiraUserPreferences(u4).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
    }

    public void testGetDisplayName()
    {
        GroupDropdown gd = new GroupDropdown(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("Group", gd.getDisplayName());
    }

    public void testGetType()
    {
        GroupDropdown gd = new GroupDropdown(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("group", gd.getType());
    }

    public void testGetGroups()
    {
        GroupDropdown gd = new GroupDropdown(ComponentAccessor.getJiraAuthenticationContext());
        Collection groups = gd.getGroups();
        assertEquals(2, groups.size());
        List<String> names = new ArrayList<String>();
        for (Object group : groups)
        {
            names.add(((com.atlassian.crowd.embedded.api.Group) group).getName());
        }
        assertTrue(names.contains(g1.getName()));
        assertTrue(names.contains(g2.getName()));
    }

    public void testGetRecipients()
    {
        GroupDropdown gd = new GroupDropdown(ComponentAccessor.getJiraAuthenticationContext());
        List recipients = gd.getRecipients(new IssueEvent(null, null, null, null), "Group 1");

        //        List recipients = gd.getRecipients(null, null, "Group 1", null);
        assertEquals(3, recipients.size());
        assertTrue(recipients.contains(new NotificationRecipient(u1)));
        assertTrue(recipients.contains(new NotificationRecipient(u2)));
        assertTrue(recipients.contains(new NotificationRecipient(u3)));

        NotificationRecipient nr1 = (NotificationRecipient) recipients.get(0);
        NotificationRecipient nr2 = (NotificationRecipient) recipients.get(1);
        NotificationRecipient nr3 = (NotificationRecipient) recipients.get(2);

        int found = 0;
        for (int i = 0; i < recipients.size(); i++)
        {
            NotificationRecipient notificationRecipient = (NotificationRecipient) recipients.get(i);
            if ("owen@atlassian.com".equals(nr1.getEmail()) || "mike@atlassian.com".equals(nr1.getEmail()) || "scott@atlassian.com".equals(nr1.getEmail()))
            {
                found++;
            }
            else
            {
                fail("Could not find one of the NoficationRecipients");
            }
            assertEquals(i + 1, found);
        }
        assertEquals(3, found);
    }
}
