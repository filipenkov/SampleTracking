/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mock.event;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.listeners.mail.MailListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.user.util.UserManager;

public class MockMailListener extends MailListener
{
    Long eventTypeIDCalled;
    String userMailCalled;
    private boolean sendMessage = false;

    public MockMailListener(NotificationSchemeManager notificationSchemeManager, IssueMailQueueItemFactory issueMailQueueItemFactory, UserManager userManager)
    {
        super(notificationSchemeManager, issueMailQueueItemFactory, userManager);
    }

    public void sendNotification(IssueEvent event)
    {
        this.eventTypeIDCalled = event.getEventTypeId();

        if (sendMessage)
            super.sendNotification(event);
    }

    public Long getEventTypeIDCalled()
    {
        return eventTypeIDCalled;
    }

    public void setSendMessage(boolean sendMessage)
    {
        this.sendMessage = sendMessage;
    }

    // UserEventListener implementation --------------------------------------------------------------------------------
    public void sendUserMail(UserEvent event, String subject, String subjectKey, String template)
    {
        this.userMailCalled = subject;
        super.sendUserMail(event, subject, subjectKey, template);
    }

    public String getUserMailCalled()
    {
        return userMailCalled;
    }
}
