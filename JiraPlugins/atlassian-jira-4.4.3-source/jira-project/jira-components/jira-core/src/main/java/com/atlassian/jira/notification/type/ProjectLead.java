/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.core.user.UserUtils;
import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.opensymphony.user.EntityNotFoundException;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collections;
import java.util.List;

public class ProjectLead extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(ProjectLead.class);
    private JiraAuthenticationContext jiraAuthenticationContext;

    public ProjectLead(JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public List getRecipients(IssueEvent event, String argument)
    {
        try
        {
            GenericValue project = event.getIssue().getProject();
            User u = UserUtils.getUser(project.getString("lead"));
            if (u == null)
            {
                return Collections.EMPTY_LIST;
            }
            else
            {
                return EasyList.build(new NotificationRecipient(u));
            }
        }
        catch (EntityNotFoundException e)
        {
            log.error("Error getting reporter notification recipients: "+e, e);
        }
        return Collections.EMPTY_LIST;
    }

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.project.lead");
    }
}
