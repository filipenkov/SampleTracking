package com.atlassian.jira.action.issue;

import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.event.issue.IssueEvent;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.util.SimpleErrorCollection;
import webwork.action.ActionContext;

/**
 * @deprecated since v4.1
 */
public class IssueDelete extends AbstractIssueAction implements IssueDeleteInterface
{
    private final IssueManager issueManager;
    private final IssueFactory issueFactory;
    private final IssueService issueService;
    private boolean permissionOverride = false;

    public IssueDelete(IssueService issueService, IssueManager issueManager, IssueFactory issueFactory)
    {
        this.issueService = issueService;
        this.issueManager = issueManager;
        this.issueFactory = issueFactory;
    }

    protected String doExecute() throws RemoveException, PermissionException
    {
        final boolean sendMail = isSendMail();

        // We always default to the ISSUE_UPDATED event type
        EventDispatchOption currEventTypeOption = (isDispatchEvent()) ? EventDispatchOption.ISSUE_DELETED : EventDispatchOption.DO_NOT_DISPATCH;

        MutableIssue issue = issueFactory.getIssue(getIssue());

        if (permissionOverride)
        {
            issueManager.deleteIssue(getLoggedInUser(), issue, currEventTypeOption, sendMail);
        }
        else
        {
            SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
            issueService.delete(getLoggedInUser(), new IssueService.DeleteValidationResult(issue, simpleErrorCollection), currEventTypeOption, sendMail);
            if (simpleErrorCollection.hasAnyErrors())
            {
                if (simpleErrorCollection.getErrors() != null)
                {
                    addErrors(simpleErrorCollection.getErrors());
                }
                if (simpleErrorCollection.getErrorMessages() != null)
                {
                    addErrorMessages(simpleErrorCollection.getErrorMessages());
                }
            }
        }

        return getResult();
    }

    private boolean isSendMail()
    {
        final Boolean sendEmailParam = (Boolean) ActionContext.getParameters().get(IssueEvent.SEND_MAIL);
        return sendEmailParam == null || sendEmailParam.booleanValue();
    }

    public void setPermissionOverride(boolean permissionOverride)
    {
        this.permissionOverride = permissionOverride;
    }
}
