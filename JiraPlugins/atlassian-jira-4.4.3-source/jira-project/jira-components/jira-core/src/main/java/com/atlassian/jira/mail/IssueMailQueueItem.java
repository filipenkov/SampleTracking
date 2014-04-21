/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.core.ofbiz.util.OFBizPropertyUtils;
import com.atlassian.core.util.StringUtils;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.ProjectKeys;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.TemplateManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.mail.MailException;
import com.atlassian.mail.MailFactory;
import com.atlassian.mail.MailThreader;
import com.atlassian.mail.queue.AbstractMailQueueItem;
import com.atlassian.mail.server.SMTPMailServer;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class IssueMailQueueItem extends AbstractMailQueueItem
{
    private static final Logger log = Logger.getLogger(IssueMailQueueItem.class);

    private final TemplateContextFactory templateContextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final MailingListCompiler mailingListCompiler;
    private final TemplateManager templateManager;

    IssueEvent event;
    Long templateId;
    Set<NotificationRecipient> recipientList;
    String notificationType;

    /**
     * Create an issue mail queue item.
     *
     * @param templateContextFactory template context factory
     * @param event the event that is the subject of this mail item.
     * @param templateId the template ID for this mail item.
     * @param recipientList a list of recipients for this mail item.
     * @param notificationType notification type
     * @param authenticationContext authentication context
     * @param mailingListCompiler mailing list compiler
     * @see IssueMailQueueItemFactory
     */
    public IssueMailQueueItem(final TemplateContextFactory templateContextFactory, final IssueEvent event, final Long templateId, final Set<NotificationRecipient> recipientList, final String notificationType, final JiraAuthenticationContext authenticationContext, final MailingListCompiler mailingListCompiler, final TemplateManager templateManager)
    {
        this.templateContextFactory = templateContextFactory;
        this.event = event;
        this.templateId = templateId;
        this.recipientList = recipientList;
        this.notificationType = notificationType;
        this.authenticationContext = authenticationContext;
        this.mailingListCompiler = mailingListCompiler;
        this.templateManager = templateManager;
    }

    /**
     * This is the subject as displayed in the Mail Queue Admin page. The subject is displayed in the preference
     * language of the current user viewing items to be sent (i.e. different from items CURRENTLY being sent).
     * <p/>
     * The subject will be displayed in the preference language of the mail recipient once the mail is actually being
     * sent. When the mail is being sent, it is a SingleMailQueueItem.
     *
     * @return String   the subject as displayed on the mail queue admin page
     */
    @Override
    public String getSubject()
    {
        final I18nHelper i18n = authenticationContext.getI18nHelper();
        try {
            String subjectTemplate = templateManager.getTemplateContent(templateId, "subject");
            final Map<String, Object> contextParams = getIssueContextParams(event);
            contextParams.put("i18n", i18n);
            contextParams.put("eventTypeName", ((TemplateContext)contextParams.get("context")).getEventTypeName(i18n));
            // Provide an OutlookDate formatter with the users locale
            OutlookDate formatter = new OutlookDate(i18n.getLocale());
            contextParams.put("dateformatter", formatter);
            return ManagerFactory.getVelocityManager().getEncodedBodyForContent(subjectTemplate, (String) event.getParams().get("baseurl"), contextParams);
        }
        catch (Exception e)
        {
            log.error("Could not determine subject", e);
            return i18n.getText("bulk.bean.initialise.error");
        }
    }

    public IssueEvent getEvent()
    {
        return event;
    }

    public Long getTemplateId()
    {
        return templateId;
    }

    public void send() throws MailException
    {
        incrementSendCount();

        final Issue issue = event.getIssue();

        try
        {
            final SMTPMailServer smtp = MailFactory.getServerManager().getDefaultSMTPMailServer();

            if (smtp == null)
            {
                log.warn("There is no default SMTP Mail server so mail cannot be sent");
            }
            else
            {
                if (issue == null)
                {
                    throw new MailException("Notification not sent; issue no longer exists [event=" + event + "]");
                }
                final GenericValue project = issue.getProject();
                final String sender = OFBizPropertyUtils.getPropertySet(project).getString(ProjectKeys.EMAIL_SENDER);

                if (!recipientList.isEmpty())
                {
                    // Ensure that issue level security is respected
                    for (final Iterator iterator = recipientList.iterator(); iterator.hasNext();)
                    {
                        final NotificationRecipient recipient = (NotificationRecipient) iterator.next();
                        if (!ManagerFactory.getPermissionManager().hasPermission(Permissions.BROWSE, issue, recipient.getUser()))
                        {
                            iterator.remove();
                        }
                    }

                    final MailThreader threader = new JiraMailThreader(event.getEventTypeId(), issue.getLong("id"));
                    Map<String, Object> contextParams = getIssueContextParams(event);
                    mailingListCompiler.sendLists(recipientList, sender, getSenderFrom(event, contextParams), templateId,
                        (String) event.getParams().get("baseurl"), contextParams, threader);
                }
            }
        }
        catch (final MailException me)
        {
            throw me;
        }
        catch (final Exception ex)
        {
            log.error(ex.getMessage(), ex);
            throw new MailException(ex.getMessage(), ex);
        }
    }

    /**
     * Return from address ('Joe Bloggs (JIRA)' usually).
     *
     *
     * @param event issue event
     * @return sender's address
     */
    private String getSenderFrom(final IssueEvent event, Map<String, Object> contextParams)
    {
        String from = ManagerFactory.getApplicationProperties().getDefaultBackedString(APKeys.EMAIL_FROMHEADER_FORMAT);
        if (from == null)
        {
            return null;
        }
        final User user = event.getRemoteUser();

        String name;

        if (user == null)
        {
            name = "Anonymous";
        }
        else
        {
            try
            {
                final String fullName = user.getDisplayName();
                if (org.apache.commons.lang.StringUtils.isBlank(fullName))
                {
                    name = user.getName();
                }
                else
                {
                    name = fullName;
                }
            }
            catch (final Exception exception)
            {
                // this should never fail, but incase it does we don't want to imply it was a anonymous user.
                try
                {
                    name = user.getName();
                }
                catch (final Exception exception2)
                {
                    name = "";
                }
            }
        }

        String email;
        try
        {
            email = (user != null ? user.getEmailAddress() : "");
        }
        catch (final Exception exception)
        {
            email = "";
        }
        final String hostname = ((user != null) && (email != null) ? email.substring(email.indexOf("@") + 1) : "");

        from = StringUtils.replaceAll(from, "${fullname}", name);
        from = StringUtils.replaceAll(from, "${email}", email);
        from = StringUtils.replaceAll(from, "${email.hostname}", hostname);
        return from;
    }

    @Override
    public String toString()
    {
        final Issue issue = event.getIssue();
        final String issueString = new ToStringBuilder(issue).append("id", issue.getId()).append("summary", issue.getSummary()).append("key",
            issue.getKey()).append("created", issue.getCreated()).append("updated", issue.getUpdated()).append("assignee", issue.getAssignee()).append(
            "reporter", issue.getReporter()).toString();
        return new ToStringBuilder(this).append("issue", issueString).append("remoteUser", event.getRemoteUser()).append("notificationType",
            notificationType).append("eventTypeId", event.getEventTypeId().longValue()).append("templateId", templateId.longValue()).toString();
    }

    protected Map<String, Object> getIssueContextParams(final IssueEvent iEvent) throws GenericEntityException
    {
        final Map<String, Object> contextParams = new HashMap<String, Object>();

        // NOTE: if adding a parameter here please update the doc online at
        // http://confluence.atlassian.com/display/JIRA/Velocity+Context+for+Email+Templates

        final TemplateContext templateContext = templateContextFactory.getTemplateContext(iEvent);
        contextParams.putAll(templateContext.getTemplateParams());

        return JiraMailQueueUtils.getContextParamsBody(contextParams);
    }

    /**
     * Used in testing only
     *
     * @return recipientList   the set of recipients to recieve this email notification
     */
    public Set getRecipientList()
    {
        return recipientList;
    }
}
