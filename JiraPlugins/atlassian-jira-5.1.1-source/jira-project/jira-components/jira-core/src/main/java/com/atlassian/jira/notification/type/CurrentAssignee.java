/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.notification.type;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.issue.EventUtils;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.notification.NotificationRecipient;
import com.atlassian.jira.security.JiraAuthenticationContext;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CurrentAssignee extends AbstractNotificationType
{
    private static final Logger log = Logger.getLogger(CurrentAssignee.class);
    private final ApplicationProperties applicationProperties;
    private JiraAuthenticationContext jiraAuthenticationContext;

    public CurrentAssignee(JiraAuthenticationContext jiraAuthenticationContext, ApplicationProperties applicationProperties)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.applicationProperties = applicationProperties;
    }

    /**
     * @return true if the notification strategy of pre JRA-6344 is in play
     */
    boolean isPostJRA6344inPlay()
    {
        // we want a missing value to be true.  So people that upgrade but keep their old jira-applications.properties
        // will by default get the new behaviour.  They have to explicitly opt out to the old behavior.
        String value = applicationProperties.getDefaultBackedString(APKeys.JIRA_ASSIGNEE_CHANGE_IS_SENT_TO_BOTH_PARTIES);
        if (StringUtils.isBlank(value ))
        {
            return true; // new behaviour in this case
        }
        return Boolean.valueOf(value);
    }

    public List<NotificationRecipient> getRecipients(IssueEvent event, String argument)
    {
        final List<NotificationRecipient> recipients;

        Issue issue = event.getIssue();
        if (issue != null)
        {
            // are we using the new assignee notification strategy
            if (isPostJRA6344inPlay())
            {
                recipients = notificationStategyPostJRA6344(event, issue);
            }
            else
            {
                recipients = notificationStategyPreJRA6344(event, issue);
            }
        }
        else
        {
            log.error("Error getting assignee notification recipients - no issue associated with event: " + event.getEventTypeId());
            recipients = Collections.emptyList();
        }
        return recipients;
    }

    /**
     * This is the notification strategy that was in place before JRA-6344 was addressed.  The previous assignee was
     * notified ONLY if the event type was "Issue Assigned".
     *
     * @param event the issue event in play
     * @param issue the issue in play
     * @return a List of NotificationRecipients
     */
    List<NotificationRecipient> notificationStategyPreJRA6344(IssueEvent event, Issue issue)
    {
        List<NotificationRecipient> recipients = new ArrayList<NotificationRecipient>();
        //if the event type is Issue Assigned then notify the previous assignee as well
        if (EventType.ISSUE_ASSIGNED_ID.equals(event.getEventTypeId()))
        {
            addPreviousAssignee(event, recipients);
        }

        addCurrentAssignee(issue, recipients);
        return recipients;
    }

    /**
     * This is the notification strategy that is in place since JRA-6344 was addressed.  The previous assignee is always
     * notified if the assignee changes.
     *
     * @param event the issue event in play
     * @param issue the issue in play
     * @return a List of NotificationRecipients
     */
    List<NotificationRecipient> notificationStategyPostJRA6344(IssueEvent event, Issue issue)
    {
        List<NotificationRecipient> recipients = new ArrayList<NotificationRecipient>();
        addPreviousAssignee(event, recipients);
        addCurrentAssignee(issue, recipients);
        return recipients;
    }

    /**
     * Adds the issues current assignee to the list if there is one
     *
     * @param issue the issue in play
     * @param recipients the list of possible recipients
     */
    private void addCurrentAssignee(final Issue issue, final List<NotificationRecipient> recipients)
    {
        User u = issue.getAssigneeUser();
        if (u != null)
        {
            recipients.add(new NotificationRecipient(u));
        }
    }

    /**
     * Adds the previous assignee of an issue change if there is one
     * 
     * @param event the issue change event
     * @param recipients the list of possible recipients
     */
    private void addPreviousAssignee(final IssueEvent event, final List<NotificationRecipient> recipients)
    {
        User previousAssignee = getPreviousAssignee(event);
        if (previousAssignee != null)
        {
            recipients.add(new NotificationRecipient(previousAssignee));
        }
    }

    /**
     * Designed to be overrriden for testing.  Gets the previous assignee by looking
     * in change history.
     *
     * TODO this could be improved by carrying the changes themselves in the IssueEvent instead of a change group GV and hence a DB interaction is required
     *
     * @param event the issue event in play
     * @return a previous assignee or null if there isnt one
     */
    ///CLOVER:OFF
    protected User getPreviousAssignee(final IssueEvent event)
    {
        return EventUtils.getPreviousAssignee(event);
    }
    ///CLOVER:ON

    public String getDisplayName()
    {
        return jiraAuthenticationContext.getI18nHelper().getText("admin.notification.types.current.assignee");
    }
}
