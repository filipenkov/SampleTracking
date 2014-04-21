package com.atlassian.jira.issue.managers;

import com.atlassian.core.ofbiz.association.AssociationManager;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.IssueIndexManager;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.util.List;
import java.util.Map;

public class DefaultIssueDeleteHelper implements IssueDeleteHelper
{
    private static final Logger log = Logger.getLogger(DefaultIssueDeleteHelper.class);

    private final IssueIndexManager indexManager;
    private final SubTaskManager subTaskManager;
    private final IssueLinkManager issueLinkManager;
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final MailThreadManager mailThreadManager;
    private final CustomFieldManager customFieldManager;
    private final AttachmentManager attachmentManager;
    private final IssueManager issueManager;
    private final AssociationManager associationManager;
    private final WorkflowManager workflowManager;
    private final ChangeHistoryManager changeHistoryManager;
    private final IssueEventManager issueEventManager;

    public DefaultIssueDeleteHelper(IssueIndexManager indexManager, SubTaskManager subTaskManager,
            IssueLinkManager issueLinkManager, RemoteIssueLinkManager remoteIssueLinkManager, MailThreadManager mailThreadManager, CustomFieldManager customFieldManager,
            AttachmentManager attachmentManager, IssueManager issueManager, AssociationManager associationManager,
            WorkflowManager workflowManager, ChangeHistoryManager changeHistoryManager, IssueEventManager issueEventManager)
    {
        this.indexManager = indexManager;
        this.subTaskManager = subTaskManager;
        this.issueLinkManager = issueLinkManager;
        this.remoteIssueLinkManager = remoteIssueLinkManager;
        this.mailThreadManager = mailThreadManager;
        this.customFieldManager = customFieldManager;
        this.attachmentManager = attachmentManager;
        this.issueManager = issueManager;
        this.associationManager = associationManager;
        this.workflowManager = workflowManager;
        this.changeHistoryManager = changeHistoryManager;
        this.issueEventManager = issueEventManager;
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        try
        {
            final Long issueId = issue.getId();
            final GenericValue issueGV = issue.getGenericValue();
            if (issueGV == null)
            {
                throw new IllegalArgumentException("The provided issue has a null GenericValue.");
            }

            DeletedIssueEventData eventData;
            if (eventDispatchOption.isEventBeingSent())
            {
                eventData = new DeletedIssueEventData(issueId);
            }
            else
            {
                eventData = new DeletedIssueEventData();
            }

            Transaction txn = Txn.begin();
            try
            {

                // remove actions
                issueGV.removeRelated("ChildAction");
                // TODO: move this into the worklog manager remove worklogs
                issueGV.removeRelated("ChildWorklog");
                    
                // Remove issue's sub-tasks (if any exist)
                removeSubTasks(user, issue, eventDispatchOption, sendMail);
                removeIssueLinks(user, issue);

                changeHistoryManager.removeAllChangeItems(issue);
                removeAttachments(issue);
                associationManager.removeAssociationsFromSource(issueGV);
                associationManager.removeUserAssociationsFromSink(issueGV);
                customFieldManager.removeCustomFieldValues(issueGV);
                workflowManager.removeWorkflowEntries(issueGV);
                issueGV.remove();
                removeNotifications(issueId);

                txn.commit();

                deindex(issue);
                dispatchDeleteEvent(user, eventDispatchOption, sendMail, eventData);
            }
            finally
            {
                txn.finallyRollbackIfNotCommitted();
            }
        }

        catch (RemoveException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new RemoveException(e);
        }
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
        deleteIssue(null, issue, EventDispatchOption.DO_NOT_DISPATCH, false);
    }

    private void removeIssueLinks(User user, Issue issue) throws RemoveException
    {
        // test if the issue is a sub-task
        // NOTE: This has to be done BEFORE removing the issue link as the sub-task issue link is
        // used to determine if the issue is a sub-task
        if (issue.isSubTask())
        {
            // Get the parent issue before removing the links, as we need the link to determine the parent issue
            Issue parentIssue = issue.getParentObject();
            // Remove the links
            issueLinkManager.removeIssueLinksNoChangeItems(issue);
            // We need to reorder the parent's links as the its sub-task link for this issue has been removed
            subTaskManager.resetSequences(parentIssue);
        }
        else
        {
            // If there are no sub-tasks so all we need to do is delete the issue's links
            issueLinkManager.removeIssueLinksNoChangeItems(issue);
        }

        for (RemoteIssueLink remoteIssueLink : remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue))
        {
            remoteIssueLinkManager.removeRemoteIssueLink(remoteIssueLink.getId(), user);
        }
    }

    private void removeAttachments(Issue issue) throws RemoveException
    {
        for (Attachment attachment : attachmentManager.getAttachments(issue))
        {
            attachmentManager.deleteAttachment(attachment);
        }
        attachmentManager.deleteAttachmentDirectory(issue);
    }

    private void removeNotifications(Long issueId)
    {
        if (issueId != null)
        {
            try
            {
                mailThreadManager.removeAssociatedEntries(issueId);
            }
            catch (DataAccessException e)
            {
                log.error("Error removing Notification Instance records for issue with id '" + issueId + "': " + e, e);
            }
        }
    }

    protected void removeSubTasks(User user, Issue parentIssue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws Exception
    {
        for (IssueLink subTaskIssueLink : subTaskManager.getSubTaskIssueLinks(parentIssue.getId()))
        {
            Issue subTaskIssue = subTaskIssueLink.getDestinationObject();
            log.debug("Deleting sub-task issue with key: " + subTaskIssue.getKey());
            deleteIssue(user, subTaskIssue, eventDispatchOption, sendMail);
            log.debug("Deleted sub-task issue with key: " + subTaskIssue.getKey());
        }
    }

    private void deindex(Issue issue)
    {
        try
        {
            indexManager.deIndex(issue);
        }
        catch (Throwable e)
        {
            log.error("Error deindexing issue: [" + issue.getKey() + "] " + issue.getSummary() + ":" + e, e);
        }
    }

    private void dispatchDeleteEvent(User user, EventDispatchOption eventDispatchOption, boolean sendMail,
            DeletedIssueEventData eventData)
    {
        if (eventDispatchOption.isEventBeingSent())
        {
            issueEventManager.dispatchEvent(eventDispatchOption.getEventTypeId(), eventData.issue,
                    eventData.paramsMap(), user, sendMail);
        }
    }

    /**
     * <p/>
     * Holds the state of the deleted issue object more consistent before we it gets deleted.
     * Makes parentId (and thus information, if the issue is a sub-task) accessible in thread local cache and
     * collects custom fields values (also making them accessible in thread-local cache).
     *
     * <p/>
     * See also:<br>
     * http://jira.atlassian.com/browse/JRA-12091<br>
     * http://jira.atlassian.com/browse/JRA-24331<br>
     * http://jira.atlassian.com/browse/JRA-21646
     */
    private class DeletedIssueEventData
    {
        private final Issue issue;
        private final Map<String, Object> customFieldValues;
        private final List<User> watchers;

        DeletedIssueEventData(Long issueId)
        {
            issue = issueManager.getIssueObject(issueId);
            // don't remove, this inits thread-local cache!
            issue.getParentId();
            customFieldValues = collectCustomFieldValues();
            watchers = issueManager.getWatchers(issue);
        }

        DeletedIssueEventData()
        {
            issue = null;
            customFieldValues = null;
            watchers = null;
        }

        private Map<String, Object> collectCustomFieldValues()
        {
            ImmutableMap.Builder<String, Object> answerBuilder = ImmutableMap.builder();
            for (CustomField customField : customFieldManager.getCustomFieldObjects(issue))
            {
                Object value = customField.getValue(issue);
                if (value != null)
                {
                    answerBuilder.put(customField.getId(), value);
                }

            }
            return answerBuilder.build();
        }

        private Map<String, Object> paramsMap()
        {
            Map<String, Object> builder = Maps.newHashMap();
            if (customFieldValues != null)
            {
                builder.put(IssueEvent.CUSTOM_FIELDS_PARAM_NAME, customFieldValues);
            }
            if (watchers != null)
            {
                builder.put(IssueEvent.WATCHERS_PARAM_NAME, watchers);
            }
            return !builder.isEmpty() ? ImmutableMap.copyOf(builder) : null;
        }
    }

}
