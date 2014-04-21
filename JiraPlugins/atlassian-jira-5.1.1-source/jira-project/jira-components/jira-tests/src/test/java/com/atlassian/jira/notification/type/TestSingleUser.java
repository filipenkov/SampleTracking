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
import com.atlassian.jira.notification.type.SingleUser;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSingleUser extends AbstractUsersTestCase
{
    public TestSingleUser(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        User u1 = createMockUser("Group Member 1", "", "owen@atlassian.com");
        new JiraUserPreferences(u1).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);
    }

    public void testGetDisplayName()
    {
        SingleUser gd = new SingleUser(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("Single User", gd.getDisplayName());
    }

    public void testGetType()
    {
        SingleUser gd = new SingleUser(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("user", gd.getType());
    }

    public void testGetRecipients()
    {
        SingleUser gd = new SingleUser(ComponentAccessor.getJiraAuthenticationContext());
        List recipients = gd.getRecipients(new IssueEvent(null, null, null, null), "Group Member 1");

        //        List recipients = gd.getRecipients(null, null, "Group Member 1", null);
        assertEquals(1, recipients.size());

        NotificationRecipient nr = (NotificationRecipient) recipients.get(0);
        assertEquals("owen@atlassian.com", nr.getEmail());
        assertTrue(nr.isHtml());
    }

    public void testDoValidation()
    {
        SingleUser gd = new SingleUser(ComponentAccessor.getJiraAuthenticationContext());
        Map params = new HashMap();
        boolean result = gd.doValidation("Single_User", params);
        assertTrue(!result);
        params.put("Single_User", null);
        result = gd.doValidation("Single_User", params);
        assertTrue(!result);
        params.put("Single_User", "Non User");
        result = gd.doValidation("Single_User", params);
        assertTrue(!result);
        params.put("Single_User", "Group Member 1");
        result = gd.doValidation("Single_User", params);
        assertTrue(result);
    }
}
