package com.atlassian.jira.mail;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.mail.queue.MailQueue;
import com.atlassian.mail.queue.MailQueueItem;
import com.google.common.collect.Maps;

import java.util.Locale;
import java.util.Map;

/**
 * Default implementation of {@link MailService}.
 *
 * @since v5.0
 */
public class MailServiceImpl implements MailService
{
    private final ApplicationProperties applicationProperties;
    private final MailQueue mailQueue;
    private final TemplateContextFactory templateContextFactory;

    public MailServiceImpl(ApplicationProperties applicationProperties, MailQueue mailQueue,
            TemplateContextFactory templateContextFactory)
    {
        this.applicationProperties = applicationProperties;
        this.mailQueue = mailQueue;
        this.templateContextFactory = templateContextFactory;
    }

    @Override
    public void sendRenderedMail(User replyTo, NotificationRecipient recipient, String subjectTemplatePath,
            String bodyTemplatePath, Map<String, Object> context)
    {
        final Locale locale = getLocale(recipient);
        final TemplateContext templateContext;
        String projectEmail = null;
        if (context.containsKey("issue"))
        {
            final Issue issue = (Issue) context.get("issue");
            projectEmail = JiraMailUtils.getProjectEmailFromIssue(issue);
            // Need to create an IssueEvent because currently that's what TemplateContextFactory expects. Not touching that!
            final IssueEvent issueEvent = new IssueEvent(issue, Maps.newHashMap(), replyTo, 0L);
            templateContext = templateContextFactory.getTemplateContext(locale, issueEvent);
        }
        else
        {
            templateContext = templateContextFactory.getTemplateContext(locale);
        }

        final Map<String, Object> templateParams = templateContext.getTemplateParams();
        templateParams.putAll(context);

        Email email = new Email(recipient.getEmail());
        String format = recipient.getFormat();
        email.setMimeType(getMimeTypeForFormat(format));

        email.setFrom(projectEmail);
        email.setFromName(JiraMailUtils.getFromNameForUser(replyTo));
        email.setReplyTo(replyTo.getEmailAddress());

        MailQueueItem item = new RenderingMailQueueItem(email, subjectTemplatePath, bodyTemplatePath, templateParams);
        mailQueue.addItem(item);
    }

    private Locale getLocale(final NotificationRecipient recipient)
    {
        final User toUser = recipient.getUserRecipient();
        if (toUser != null)
        {
            return I18nBean.getLocaleFromUser(toUser);
        }
        return applicationProperties.getDefaultLocale();
    }


    private static String getMimeTypeForFormat(String format)
    {
        if ("html".equals(format))
        {
            return "text/html";
        }
        if ("text".equals(format))
        {
            return "text/plain";
        }
        return format; // HACK: until we can look up the format -> mimetype mapping, allow users to specify a MIME type directly.
    }
}
