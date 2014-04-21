package com.atlassian.jira.event.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.util.I18nHelper;
import org.apache.log4j.Logger;

import java.util.Map;

/**
 * Subclasses of IssueEventListener can now override relevant functions in the interface as opposed to having to provide
 * a dummy implementation for all methods.
 */
public abstract class AbstractIssueEventListener implements IssueEventListener
{
    private static final Logger log = Logger.getLogger(AbstractIssueEventListener.class);

    public void issueCreated(final IssueEvent event)
    {}

    public void issueUpdated(final IssueEvent event)
    {}

    public void issueAssigned(final IssueEvent event)
    {}

    public void issueResolved(final IssueEvent event)
    {}

    public void issueClosed(final IssueEvent event)
    {}

    public void issueCommented(final IssueEvent event)
    {}

    public void issueCommentEdited(final IssueEvent event)
    {}

    public void issueWorklogUpdated(final IssueEvent event)
    {}

    public void issueWorklogDeleted(final IssueEvent event)
    {}

    public void issueReopened(final IssueEvent event)
    {}

    public void issueDeleted(final IssueEvent event)
    {}

    public void issueWorkLogged(final IssueEvent event)
    {}

    public void issueStarted(final IssueEvent event)
    {}

    public void issueStopped(final IssueEvent event)
    {}

    public void issueMoved(final IssueEvent event)
    {}

    public void issueGenericEvent(final IssueEvent event)
    {}

    /**
     * Determines how the event should be processed. Based on the event type ID within the event, the appropriate
     * actions are called.
     * <p/>
     * An event with an unknown event type ID is logged and discarded.
     * <p/>
     * The customEvent method should be implemented to deal with any custom events that are added to the system
     *
     * @param event - the IssueEvent object containing the event type ID
     */
    public void workflowEvent(final IssueEvent event)
    {
        final EventTypeManager eventTypeManager = ComponentAccessor.getEventTypeManager();

        final Long eventTypeId = event.getEventTypeId();
        final EventType eventType = eventTypeManager.getEventType(eventTypeId);

        if (eventType == null)
        {
            log.error("Issue Event Type with ID '" + eventTypeId + "' is not recognised.");
        }
        else if (eventTypeId.equals(EventType.ISSUE_CREATED_ID))
        {
            issueCreated(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_UPDATED_ID))
        {
            issueUpdated(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_ASSIGNED_ID))
        {
            issueAssigned(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_RESOLVED_ID))
        {
            issueResolved(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_COMMENTED_ID))
        {
            issueCommented(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_COMMENT_EDITED_ID))
        {
            issueCommentEdited(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_CLOSED_ID))
        {
            issueClosed(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_REOPENED_ID))
        {
            issueReopened(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_DELETED_ID))
        {
            issueDeleted(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_MOVED_ID))
        {
            issueMoved(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_WORKLOGGED_ID))
        {
            issueWorkLogged(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_WORKSTARTED_ID))
        {
            issueStarted(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_WORKSTOPPED_ID))
        {
            issueStopped(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_WORKLOG_UPDATED_ID))
        {
            issueWorklogUpdated(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_WORKLOG_DELETED_ID))
        {
            issueWorklogDeleted(event);
        }
        else if (eventTypeId.equals(EventType.ISSUE_GENERICEVENT_ID))
        {
            issueGenericEvent(event);
        }
        else
        {
            customEvent(event);
        }
    }

    /**
     * Implement this method to deal with any custom events within the system
     *
     * @param event IssueEvent
     */
    public void customEvent(final IssueEvent event)
    {}

    public void init(final Map params)
    {}

    public String[] getAcceptedParams()
    {
        return new String[0];
    }

    public boolean isInternal()
    {
        return false;
    }

    public boolean isUnique()
    {
        return false;
    }

    public String getDescription()
    {
        return null;
    }

    protected I18nHelper getI18NBean()
    {
        return ComponentAccessor.getJiraAuthenticationContext().getI18nHelper();
    }
}
