/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.JiraEvent;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.user.util.UserManager;

/**
 * This listener is used to debug the MailListener.
 * <p>
 * Basically instead of actually sending an email, it will print the method call
 *
 * @see MailListener
 */
public class DebugMailListener extends MailListener
{
    public DebugMailListener(NotificationSchemeManager notificationSchemeManager, IssueMailQueueItemFactory issueMailQueueItemFactory, UserManager userManager)
    {
        super(notificationSchemeManager, issueMailQueueItemFactory, userManager);
    }

    protected void sendNotification(IssueEvent event)
    {
        logEvent(event);
    }

    protected void sendUserMail(UserEvent event, String subject, String template)
    {
        logEvent(event);
        log("Subject: " + subject);
        log("Template: " + template);
    }

    /**
     * This is duplicated code from debugListener
     * @param event
     */
    private void logEvent(JiraEvent event)
    {
        try
        {
            if (event instanceof IssueEvent)
            {
                IssueEvent issueEvent = (IssueEvent) event;
                log("Issue: [#" + issueEvent.getIssue().getLong("id") + "] " + issueEvent.getIssue().getString("summary"));
                log("Comment: " + issueEvent.getComment());
                log("Change Group: " + issueEvent.getChangeLog());
                log("EventTypeId: " + issueEvent.getEventTypeId());
            }
            else if (event instanceof UserEvent)
            {
                UserEvent userEvent = (UserEvent) event;
                log("User: " + userEvent.getUser().getName() + " (" + userEvent.getUser().getEmailAddress() + ")");
            }

            log(" Time: " + event.getTime());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void log(String msg)
    {
        System.err.println("[DebugMailListener]: " + msg);
    }

    public boolean isInternal()
    {
        return false;
    }

}
