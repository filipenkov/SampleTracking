/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Collections;
import java.util.List;

public class ProjectLead extends AbstractNotificationType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectLead(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        Project project = event.getIssue().getProjectObject();
        User u = project.getLead();
        if (u == null)
        {
            return Collections.emptyList();
        }
        else
        {
            return EasyList.build(new NotificationRecipient(u));
        }
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.project.lead");
    }
}
