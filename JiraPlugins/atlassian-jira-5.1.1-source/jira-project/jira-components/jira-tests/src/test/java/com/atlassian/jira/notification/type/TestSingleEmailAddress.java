/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.local.LegacyJiraMockTestCase;
import com.atlassian.jira.notification.NotificationRecipient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSingleEmailAddress extends LegacyJiraMockTestCase
{
    public TestSingleEmailAddress(String s)
    {
        super(s);
    }

    public void testGetDisplayName()
    {
        SingleEmailAddress gd = new SingleEmailAddress(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("Single Email Address", gd.getDisplayName());
    }

    public void testGetType()
    {
        SingleEmailAddress gd = new SingleEmailAddress(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("email", gd.getType());
    }

    public void testGetRecipients()
    {
        SingleEmailAddress gd = new SingleEmailAddress(ComponentAccessor.getJiraAuthenticationContext());
        List recipients = gd.getRecipients(new IssueEvent(null, null, null, null), "owen@atlassian.com");

        //        List recipients = gd.getRecipients(null, null, "owen@atlassian.com", null);
        assertEquals(1, recipients.size());

        NotificationRecipient nr = (NotificationRecipient) recipients.get(0);
        assertEquals("owen@atlassian.com", nr.getEmail());
        assertTrue(nr.isHtml());
    }

    public void testDoValidation()
    {
        SingleEmailAddress gd = new SingleEmailAddress(ComponentAccessor.getJiraAuthenticationContext());
        Map params = new HashMap();
        boolean result = gd.doValidation("Single_Email_Address", params);
        assertTrue(!result);
        params.put("Single_Email_Address", null);
        result = gd.doValidation("Single_Email_Address", params);
        assertTrue(!result);
        params.put("Single_Email_Address", "owen@atlassian.com");
        result = gd.doValidation("Single_Email_Address", params);
        assertTrue(result);
    }
}
