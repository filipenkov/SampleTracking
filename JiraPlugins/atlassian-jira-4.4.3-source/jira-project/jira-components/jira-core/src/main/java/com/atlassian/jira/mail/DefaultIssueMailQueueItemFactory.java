/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.mail;

import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.template.TemplateManager;

import java.util.Set;

public class DefaultIssueMailQueueItemFactory implements IssueMailQueueItemFactory
{
    private final TemplateContextFactory templateContextFactory;
    private final JiraAuthenticationContext authenticationContext;
    private final MailingListCompiler mailingListCompiler;
    private final TemplateManager templateManager;

    public DefaultIssueMailQueueItemFactory(TemplateContextFactory templateContextFactory, JiraAuthenticationContext authenticationContext, MailingListCompiler mailingListCompiler, TemplateManager templateManager)
    {
        this.templateContextFactory = templateContextFactory;
        this.authenticationContext = authenticationContext;
        this.mailingListCompiler = mailingListCompiler;
        this.templateManager = templateManager;
    }

    public IssueMailQueueItem getIssueMailQueueItem(IssueEvent event, Long templateId, Set<NotificationRecipient> recipientList, String notificationType)
    {
        return new IssueMailQueueItem(templateContextFactory, event, templateId, recipientList, notificationType, authenticationContext, mailingListCompiler, templateManager);
    }
}
