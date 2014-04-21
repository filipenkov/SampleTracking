package com.atlassian.jira.mention.commands;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.event.issue.MentionIssueEvent;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.fields.renderer.wiki.AtlassianWikiRenderer;
import com.atlassian.jira.mail.MailService;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.mail.server.MailServerManager;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Responsible for sending out a notification email to the users mentioned in an issue.
 *
 * @since v5.0
 */
@EventComponent
public class EmailMentionedUsers
{
    private Logger log = Logger.getLogger(EmailMentionedUsers.class);

    private final MailService mailService;
    private final RendererManager rendererManager;
    private final MailServerManager mailServerManager;

    public EmailMentionedUsers(final MailService mailService,
            final RendererManager rendererManager, final MailServerManager mailServerManager)
    {
        this.rendererManager = rendererManager;
        this.mailService = mailService;
        this.mailServerManager = mailServerManager;
    }

    @EventListener
    public void execute(final MentionIssueEvent mentionIssueEvent)
    {
        if (mailServerManager.isDefaultSMTPMailServerDefined())
        {
            final User from = mentionIssueEvent.getFromUser();

            for (User toUser : mentionIssueEvent.getToUsers())
            {
                if (toUser.getEmailAddress() == null)
                {
                    log.warn("User " + toUser.getName() + " does not have a registered email address. No mentioned notification will be sent.");
                    continue;
                }

                final NotificationRecipient recipient = new NotificationRecipient(toUser);

                //only send mention e-mails to users that aren't already getting a notification about this event.
                if (!mentionIssueEvent.getCurrentRecipients().contains(recipient))
                {
                    Map<String, Object> params = Maps.newHashMap();
                    params.put("comment", mentionIssueEvent.getMentionText());
                    String htmlComment = rendererManager.getRenderedContent(AtlassianWikiRenderer.RENDERER_TYPE, mentionIssueEvent.getMentionText(), mentionIssueEvent.getIssue().getIssueRenderContext());
                    params.put("htmlComment", htmlComment);
                    params.put("issue", mentionIssueEvent.getIssue());

                    String format = recipient.getFormat();
                    String subjectTemplatePath = "templates/email/subject/issuementioned.vm";
                    String bodyTemplatePath = "templates/email/" + format + "/issuementioned.vm";

                    mailService.sendRenderedMail(from, recipient, subjectTemplatePath, bodyTemplatePath, params);
                }
            }
        }
    }
}
