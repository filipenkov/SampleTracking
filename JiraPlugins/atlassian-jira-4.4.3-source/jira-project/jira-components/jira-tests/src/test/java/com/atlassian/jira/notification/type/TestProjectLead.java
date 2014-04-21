/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.type.ProjectLead;
import com.atlassian.jira.user.preferences.JiraUserPreferences;
import com.atlassian.jira.user.preferences.PreferenceKeys;
import com.atlassian.core.util.map.EasyMap;
import com.opensymphony.user.User;
import org.ofbiz.core.entity.GenericValue;
import com.atlassian.jira.local.AbstractUsersTestCase;

import java.util.List;

public class TestProjectLead extends AbstractUsersTestCase
{
    private GenericValue issue;

    public TestProjectLead(String s)
    {
        super(s);
    }

    protected void setUp() throws Exception
    {
        super.setUp();

        User u1 = UtilsForTests.getTestUser("Watcher 1");
        u1.setEmail("owen@atlassian.com");
        new JiraUserPreferences(u1).setString(PreferenceKeys.USER_NOTIFICATIONS_MIMETYPE, NotificationRecipient.MIMETYPE_HTML);

        UtilsForTests.getTestEntity("Project", EasyMap.build("id", new Long(1), "lead", "Watcher 1"));
        issue = UtilsForTests.getTestEntity("Issue", EasyMap.build("summary", "this", "project", new Long(1)));
    }

    public void testGetDisplayName()
    {
        ProjectLead pl = new ProjectLead(ComponentAccessor.getJiraAuthenticationContext());
        assertEquals("Project Lead", pl.getDisplayName());
    }

    public void testGetRecipients()
    {
        ProjectLead pl = new ProjectLead(ComponentAccessor.getJiraAuthenticationContext());

        // Create the issue object
        IssueFactory issueFactory = (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class);
        Issue issueObject = issueFactory.getIssue(issue);

        List recipients = pl.getRecipients(new IssueEvent(issueObject, null, null, null), null);

        //        List recipients = pl.getRecipients(issue, null, null, null);
        assertEquals(1, recipients.size());

        NotificationRecipient nr = (NotificationRecipient) recipients.get(0);
        assertEquals("owen@atlassian.com", nr.getEmail());
        assertTrue(nr.isHtml());
    }
}
