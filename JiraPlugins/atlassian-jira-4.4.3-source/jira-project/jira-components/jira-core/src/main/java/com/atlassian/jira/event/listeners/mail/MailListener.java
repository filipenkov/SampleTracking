/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.mail.UserMailQueueItem;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.queue.MailQueueItem;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A listener for emailing notifications.
 * <p>
 * At the moment when this listener is activated it will email the reporter, assignee and any watchers.
 * <p>
 * The content of the emails is governed by Velocity templates (in /templates) and the emails are sent
 * out by the MailSender.
 * <p>
 * Parameters:
 * email.from        - who the emails should be sent from
 * email.server        - the SMTP server to send email through
 * email.session    - the JNDI location of a MailSession to use when sending email
 * subject.prefix    - (optional) any prefix for the subject, like "[FooBar]" -> Subject: [FooBar] BazBat
 *
 * @see DebugMailListener
 */
public class MailListener extends AbstractIssueEventListener implements IssueEventListener, UserEventListener
{
    private static final Logger log = Logger.getLogger(MailListener.class);

    private final NotificationSchemeManager notificationSchemeManager;
    private final IssueMailQueueItemFactory issueMailQueueItemFactory;
    private final UserManager userManager;

    public MailListener(NotificationSchemeManager notificationSchemeManager, IssueMailQueueItemFactory issueMailQueueItemFactory, UserManager userManager)
    {
        this.notificationSchemeManager = notificationSchemeManager;
        this.issueMailQueueItemFactory = issueMailQueueItemFactory;
        this.userManager = userManager;
    }

    public void init(Map params)
    {
    }

    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    /**
     * Retrieve the assocaited notification scheme and create the mail items for notification of the specified event.
     *
     * @param event
     */
    protected void sendNotification(IssueEvent event)
    {
        if (event.isSendMail())
        {
            GenericValue projectGV = event.getIssue().getProject();
            GenericValue notificationScheme = notificationSchemeManager.getNotificationSchemeForProject(projectGV);

            if (notificationScheme != null)
                createMailItems(notificationScheme, event);
        }
    }

    /**
     * Add mail items to the mail queue for the event and the associated notification types as defined in the notification
     * scheme.
     * <p>
     * Only the first email encountered for a user will be sent in the case where a user is included in multiple notification
     * types.
     *
     * @param notificationScheme    used to determine the recipients of the notification of the specified event
     * @param event                 the cause of the notification
     */
    protected void createMailItems(GenericValue notificationScheme, IssueEvent event)
    {
        try
        {
            // Retrieve the entities related to this event type from the notification schemee
            Collection notificationSchemeEntities = notificationSchemeManager.getEntities(notificationScheme, event.getEventTypeId());

            // Retain a list of ALL recipients.
            // This set ensures that a user will only recieve one email regarding this issue event
            Set<NotificationRecipient> allRecipients = new HashSet<NotificationRecipient>();

            for (Iterator iterator = notificationSchemeEntities.iterator(); iterator.hasNext();)
            {
                GenericValue schemeEntity = (GenericValue) iterator.next();
                SchemeEntity notificationSchemeEntity = new SchemeEntity(schemeEntity.getLong("id"), schemeEntity.getString("type"), schemeEntity.getString("parameter"), schemeEntity.get("eventTypeId"), schemeEntity.get("templateId"), schemeEntity.getLong("scheme"));


                Long templateId = ComponentManager.getInstance().getTemplateManager().getTemplate(notificationSchemeEntity).getId();

                // The intended recipient list - a user will only recieve the first email encountered for them
                // Any further emails intended for that user in relation to this notification event will not be sent
                Collection<NotificationRecipient> intendedRecipients = notificationSchemeManager.getRecipients(event, notificationSchemeEntity);
                Set<NotificationRecipient> actualRecipients = new HashSet<NotificationRecipient>();

                if (intendedRecipients != null && !intendedRecipients.isEmpty())
                {
                    for (NotificationRecipient notificationRecipient : intendedRecipients)
                    {
                        // Check if the recipient is already included in the recipient list for another template for this event
                        if (!allRecipients.contains(notificationRecipient))
                        {
                            actualRecipients.add(notificationRecipient);
                            allRecipients.add(notificationRecipient);
                        }
                        else
                        {
                            log.debug("Multiple event (" + event.getEventTypeId() + ") notification emails intended for the recipient: " + notificationRecipient.getUser() + ". Sending first intended email only for the event.");
                        }
                    }

                    if (!actualRecipients.isEmpty())
                    {
                        MailQueueItem item = issueMailQueueItemFactory.getIssueMailQueueItem(event, templateId, actualRecipients, notificationSchemeEntity.getType());
                        ManagerFactory.getMailQueue().addItem(item);
                    }
                }
            }
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error accessing the notifications for the scheme: " + notificationScheme.getString("name") + ".", e);
        }
    }

    // IssueEventListener implementation -------------------------------------------------------------------------------
    public void issueCreated(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueAssigned(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueClosed(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueResolved(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueReopened(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueUpdated(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueCommented(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueCommentEdited(IssueEvent event)
    {
        sendNotification(event);    
    }

    public void issueWorkLogged(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueWorklogUpdated(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueWorklogDeleted(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueDeleted(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueMoved(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueStarted(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueStopped(IssueEvent event)
    {
        sendNotification(event);
    }

    public void issueGenericEvent(IssueEvent event)
    {
        sendNotification(event);
    }

    public void customEvent(IssueEvent event)
    {
        sendNotification(event);
    }

    // UserEventListener implementation --------------------------------------------------------------------------------
    protected void sendUserMail(UserEvent event, String subject, String subjectKey, String template)
    {
        MailQueueItem item = new UserMailQueueItem(event, subject, subjectKey, template);
        ManagerFactory.getMailQueue().addItem(item);
    }

    public void userSignup(UserEvent event)
    {
        sendUserMail(event, "Account signup", "template.user.signup.subject", "usersignup.vm");
    }

    public void userCreated(UserEvent event)
    {
        if (userManager.canUpdateUserPassword(event.getUser()))
        {
            sendUserMail(event, "Account created", "template.user.created.subject", "usercreated.vm");
        }
        else
        {
            sendUserMail(event, "Account created", "template.user.created.subject", "usercreated-nopassword.vm");
        }
    }

    public void userForgotPassword(UserEvent event)
    {
        sendUserMail(event, "Account password", "template.user.forgotpassword.subject", "forgotpassword.vm");
    }

    public void userForgotUsername(UserEvent event)
    {
        sendUserMail(event, "Account usernames", "template.user.forgotusername.subject", "forgotusernames.vm");
    }

    public void userCannotChangePassword(UserEvent event)
    {
        sendUserMail(event, "Account usernames", "template.user.cannotchangepassword.subject", "cannotchangepassword.vm");
    }

    //whether administrators can delete this listener
    public boolean isInternal()
    {
        return true;
    }

    /**
     * Mail Listeners are unique.  It would be a rare case when you would want two emails sent out.
     */
    public boolean isUnique()
    {
        return true;
    }

    public String getDescription()
    {
        return "For each user or issue event, generate an appropriate email, and send to the required participants.";
    }
}
