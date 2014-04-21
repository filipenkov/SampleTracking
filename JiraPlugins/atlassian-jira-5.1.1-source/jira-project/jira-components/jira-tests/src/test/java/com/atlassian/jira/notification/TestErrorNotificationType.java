/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.local.LegacyJiraMockTestCase;

public class TestErrorNotificationType extends LegacyJiraMockTestCase
{
    public TestErrorNotificationType(String s)
    {
        super(s);
    }

    public void testGetSets()
    {
        ErrorNotificationType ent = new ErrorNotificationType("Test Error");
        assertEquals("Test Error", ent.getDisplayName());
        assertNull(ent.getRecipients(new IssueEvent(null, null, null, null), null));

        //        assertNull(ent.getRecipients(null, null, null, null));
        assertTrue(!ent.doValidation(null, null));
    }
}
