/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Lists.transform;

public class AllWatchers extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(AllWatchers.class);

    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final IssueManager issueManager;

    public AllWatchers(JiraAuthenticationContext jiraAuthenticationContext, IssueManager issueManager)
    {
        this.jiraAuthenticationContext = checkNotNull(jiraAuthenticationContext);
        this.issueManager = checkNotNull(issueManager);
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        try
        {
            List<User> watchers = getFromEventParams(event);
            if (watchers == null)
            {
                watchers = issueManager.getWatchers(event.getIssue());
            }
            return transform(watchers, UserToRecipient.INSTANCE);
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
            return Collections.emptyList();
        }
    }

    @SuppressWarnings ( { "unchecked" })
    private List<User> getFromEventParams(IssueEvent event)
    {
        return (List) event.getParams().get(IssueEvent.WATCHERS_PARAM_NAME);
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.all.watchers");
    }
}
