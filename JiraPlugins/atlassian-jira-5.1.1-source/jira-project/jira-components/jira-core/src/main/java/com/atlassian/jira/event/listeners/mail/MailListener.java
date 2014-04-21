/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.event.listeners.mail;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.AbstractIssueEventListener;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventListener;
import com.atlassian.jira.event.user.UserEvent;
import com.atlassian.jira.event.user.UserEventListener;
import com.atlassian.jira.mail.IssueMailQueueItemFactory;
import com.atlassian.jira.mail.UserMailQueueItem;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.notification.NotificationSchemeManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.collect.Sets;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.List;
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
     * @param event the IssueEvent triggering the notification
     */
    protected void sendNotification(IssueEvent event)
    {
        if (!event.isSendMail())
            return;

        Project project = event.getProject();
        try
        {
            List<SchemeEntity> schemeEntities = notificationSchemeManager.getNotificationSchemeEntities(project, event.getEventTypeId());
            addMailItems(schemeEntities, event);
        }
        catch (GenericEntityException e)
        {
            log.error("There was an error accessing the notification scheme for the project: " + project.getKey() + ".", e);
        }
    }

    /**
     * Add mail items to the mail queue for the event and the associated notification types as defined in the notification
     * scheme.
     * <p>
     * Only the first email encountered for a user will be sent in the case where a user is included in multiple notification
     * types.
     *
     * @param schemeEntities    used to determine the recipients of the notification of the specified event
     * @param event                 the cause of the notification
     *
     * @throws GenericEntityException thrown if we can't retrieve the SchemeEntities or NotificationRecipients
     */
    protected void addMailItems(List<SchemeEntity> schemeEntities, IssueEvent event) throws GenericEntityException
    {
        // Ensure that a user only receives one email per IssueEvent.
        Set<NotificationRecipient> allRecipients = Sets.newHashSet();

        for (SchemeEntity schemeEntity : schemeEntities)
        {
            Set<NotificationRecipient> schemeRecipients = notificationSchemeManager.getRecipients(event, schemeEntity);

            schemeRecipients.removeAll(allRecipients);
            if (!schemeRecipients.isEmpty())
            {
                allRecipients.addAll(schemeRecipients);

                long templateId = ComponentManager.getInstance().getTemplateManager().getTemplate(schemeEntity).getId();
                MailQueueItem item = issueMailQueueItemFactory.getIssueMailQueueItem(event, templateId, schemeRecipients, schemeEntity.getType());

                ComponentAccessor.getMailQueue().addItem(item);
            }
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
        ComponentAccessor.getMailQueue().addItem(item);
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
