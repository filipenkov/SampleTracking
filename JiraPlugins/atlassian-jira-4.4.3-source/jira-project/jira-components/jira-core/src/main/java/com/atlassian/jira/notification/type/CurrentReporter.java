/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class CurrentReporter extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(CurrentReporter.class);
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentReporter(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List getRecipients(IssueEvent event, String argument)
    {
        Issue issue = event.getIssue();
        if (issue != null)
        {
            User u = issue.getReporter();
            String level = (String) event.getParams().get("level");

            if (u != null && (level == null || u.inGroup(level)))
            {
                return EasyList.build(new NotificationRecipient(u));
            }
            else
            {
                //Guest reported or user not in the relevent level
                return Collections.EMPTY_LIST;
            }
        }
        else
        {
            log.error("Error getting reporter notification recipients - no issue associated with event: " + event.getEventTypeId());
        }
        return Collections.EMPTY_LIST;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.reporter");
    }
}
