/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.mail.Email;
import com.atlassian.mail.MailThreader;

/**
 * Implementation of MailThreader that stores threads in an OfBiz-mediated database.
 */
public class JiraMailThreader implements MailThreader
{
    private Long eventTypeId;
    private Long issueId;

    /**
     * Constructor
     *
     * @param eventTypeId the event type ID
     * @param issueId     the issue id - the subject of the emails.
     */
    public JiraMailThreader(Long eventTypeId, Long issueId)
    {
        this.eventTypeId = eventTypeId;
        this.issueId = issueId;
    }

    public void threadEmail(Email email)
    {
        if (!eventTypeId.equals(EventType.ISSUE_CREATED_ID) && isEventTypeValid())
        {
            // Do not thread e-mail for issue creation as there is nothing to thread them with
            // (no in-reply-to message id exists)
            if (email instanceof com.atlassian.jira.mail.Email)
            {
                com.atlassian.jira.mail.Email jiraEmail = (com.atlassian.jira.mail.Email) email;
                ComponentAccessor.getMailThreadManager().threadNotificationEmail(jiraEmail, this.issueId);
            }
        }
    }

    public void storeSentEmail(Email email)
    {
        if (isEventTypeValid())
        {
            final MailThreadManager mailThreadManager = ComponentAccessor.getMailThreadManager();

            mailThreadManager.createMailThread(mailThreadManager.getThreadType(eventTypeId), this.issueId, email.getTo(), email.getMessageId());
        }
    }

    private boolean isEventTypeValid()
    {
        return ComponentAccessor.getEventTypeManager().isEventTypeExists(eventTypeId);
    }

}
