/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.core.ofbiz.CoreFactory;
import com.atlassian.core.ofbiz.util.EntityUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.issue.IssueManager;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.DelegatorInterface;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Message;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MailThreadManagerImpl implements MailThreadManager
{
    private static final Logger log = Logger.getLogger(MailThreadManagerImpl.class);

    private final DelegatorInterface genericDelegator;
    private final IssueManager issueManager;
    private final EventTypeManager eventTypeManager;

    public MailThreadManagerImpl(DelegatorInterface genericDelegator, IssueManager issueManager, EventTypeManager eventTypeManager)
    {
        this.genericDelegator = genericDelegator;
        this.issueManager = issueManager;
        this.eventTypeManager = eventTypeManager;
    }

    public MailThreadManagerImpl()
    {
        this(CoreFactory.getGenericDelegator(), ManagerFactory.getIssueManager(), ComponentAccessor.getEventTypeManager());
    }

    public void createMailThread(String type, Long source, String emailAddress, String messageId)
    {
        // Check that the message id is passed, otherwise there is nothig to record
        if (messageId == null || messageId.trim().length() <= 0)
            return;

        Map fields = new HashMap();
        fields.put("type", type);
        fields.put("source", source);
        fields.put("email", emailAddress);
        fields.put("messageid", messageId);

        try
        {
            EntityUtils.createValue("NotificationInstance", fields);
        }
        catch (GenericEntityException e)
        {
            log.warn("Unable to store thread details for email for source '" + source + "' sent to '" + emailAddress + "'.", e);
        }
    }

    public void threadNotificationEmail(Email email, Long issueId)
    {
        GenericValue notificationGV = null;
        try
        {
            // All notification e-mails a threaded "against" the ISSUE CREATED notification e-mail must be the first created
            // Sorting by id to get first as there is no date field in the table.
            notificationGV = EntityUtil.getFirst(genericDelegator.findByAnd("NotificationInstance", EasyMap.build("source", issueId, "type", NOTIFICATION_ISSUE_CREATED, "email", email.getTo()), EasyList.build("id")));
        }
        catch (GenericEntityException e)
        {
            log.info("Couldn't find Notification Instance record for issue " + issueId + ", so can't thread related email", e);
            return;
        }
        catch (Throwable t)
        {
            log.warn("Error retrieving mail thread for issue " + issueId + " to " + email.getTo() + ": " + t.getMessage(), t);
        }

        if (notificationGV == null)
        {
            log.info("Couldn't find Notification Instance record for issue " + issueId + ", so can't thread related email");
            return;
        }

        log.debug("Adding In-Reply-To: " + notificationGV.getString("messageid"));
        email.setInReplyTo(notificationGV.getString("messageid"));
    }

    public String getThreadType(Long eventTypeId)
    {
        if (EventType.ISSUE_CREATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_CREATED;
        }
        else if (EventType.ISSUE_ASSIGNED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_ASSIGNED;
        }
        else if (EventType.ISSUE_RESOLVED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_RESOLVED;
        }
        else if (EventType.ISSUE_CLOSED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_CLOSED;
        }
        else if (EventType.ISSUE_REOPENED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_REOPENED;
        }
        else if (EventType.ISSUE_COMMENTED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_COMMENTED;
        }
        else if (EventType.ISSUE_COMMENT_EDITED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_COMMENT_EDITED;
        }
        else if (EventType.ISSUE_DELETED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_DELETED;
        }
        else if (EventType.ISSUE_UPDATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_UPDATED;
        }
        else if (EventType.ISSUE_WORKLOGGED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOGGED;
        }
        else if (EventType.ISSUE_WORKLOG_UPDATED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOG_UPDATED;
        }
        else if (EventType.ISSUE_WORKLOG_DELETED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKLOG_DELETED;
        }
        else if (EventType.ISSUE_WORKSTARTED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKSTARTED;
        }
        else if (EventType.ISSUE_WORKSTOPPED_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_WORKSTOPPED;
        }
        else if (EventType.ISSUE_GENERICEVENT_ID.equals(eventTypeId))
        {
            return NOTIFICATION_ISSUE_GENERICEVENT;
        }
        else
        {
            if (eventTypeManager.isEventTypeExists(eventTypeId))
            {
                return NOTIFICATION_KEY + eventTypeId;
            }
            else
            {
                throw new IllegalArgumentException("Unable to thread this notification as the event type " + eventTypeId + " is unknown.");
            }
        }
    }

    public GenericValue getAssociatedIssue(Message message)
    {
        try
        {
            // Get the message's message id
            final String[] messageIds = message.getHeader("In-Reply-To");
            try
            {
                if (messageIds != null && messageIds.length > 0)
                {
                    String messageId = messageIds[0];

                    // Some e-mail clients append extra information to the message's massage id when
                    // setting the in-reply-to message headere. We need to strip that extra information
                    final int index = messageId.indexOf(";");
                    if (index > 0)
                    {
                        messageId = messageId.substring(0, index);
                    }

                    // The message has a message id. See if we can find any issues associated with the message-id.
                    final List notificationInstanceGVs = genericDelegator.findByAnd("NotificationInstance", EasyMap.build("messageid", messageId));
                    if (notificationInstanceGVs == null || notificationInstanceGVs.isEmpty())
                    {
                        // Cannot find any associated issues with the message id
                        log.debug("Cannot find any associated issues with message id '" + messageId + "'.");
                        return null;
                    }
                    else
                    {
                        // Found records with associated issue
                        GenericValue notificationInstanceGV = (GenericValue) notificationInstanceGVs.get(0);
                        final Long issueId = notificationInstanceGV.getLong("source");

                        // Retrieve the issue with the given issue id
                        return issueManager.getIssue(issueId);
                    }
                }
                else
                {
                    log.debug("No In-Reply-To header found");
                    return null;
                }
            }
            catch (GenericEntityException e)
            {
                log.error("Error occurred while retrieving Notification Instance records for message.");
                return null;
            }
        }
        catch (MessagingException e)
        {
            log.error("Error occurred while determining message id of an e-mail message.", e);
            return null;
        }
    }

    public int removeAssociatedEntries(Long issueId) throws GenericEntityException
    {
        return genericDelegator.removeByAnd("NotificationInstance", EasyMap.build("source", issueId));
    }
}
