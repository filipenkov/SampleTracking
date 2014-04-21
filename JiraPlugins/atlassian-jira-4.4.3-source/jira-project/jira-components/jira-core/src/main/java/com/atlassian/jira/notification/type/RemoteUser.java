/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;

import java.util.Collections;
import java.util.List;

public class RemoteUser extends AbstractNotificationType
{
    private JiraAuthenticationContext jiraAuthenticationContext;

    public RemoteUser(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List getRecipients(IssueEvent event, String argument)
    {
        if (event.getRemoteUser() == null)
        {
            //Guest user
            return Collections.EMPTY_LIST;
        }
        else
        {
            return EasyList.build(new NotificationRecipient(event.getRemoteUser()));
        }
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.current.user");
    }
}
