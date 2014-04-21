package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import org.ofbiz.core.entity.GenericEntityException;

public class DeleteLink extends AbstractIssueSelectAction
{
    private final IssueLinkManager issueLinkManager;

    private Long destId;
    private Long linkType;
    private Long sourceId;
    private IssueLink issueLink;
    private boolean confirm;

    public DeleteLink(final IssueLinkManager issueLinkManager)
    {
        this.issueLinkManager = issueLinkManager;
    }

    protected void doValidation()
    {
        try
        {
            // To delete issue links - you need to have the "edit" issue link permission.
            if (linkType != null && !isHasIssuePermission(Permissions.LINK_ISSUE, getIssue()))
            {
                addErrorMessage(getText("admin.errors.issues.no.permission.to.delete.links"));
            }

            if ((destId != null || sourceId != null) && linkType != null && getLink() == null)
            {
                addErrorMessage(getText("admin.errors.issues.cannot.find.link"));
            }
        }
        catch (Exception e)
        {
            log.error("Exception: " + e, e);
            addErrorMessage(getText("admin.errors.issues.exception.occured.validating", e));
        }
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        if (confirm)
        {
            try
            {
                issueLinkManager.removeIssueLink(getLink(), getLoggedInUser());
            }
            catch (RemoveException e)
            {
                addErrorMessage(getText("admin.errors.issues.error.occured.removing.link", e));
                log.error("An error occurred removing link: " + e, e);
                return ERROR;
            }

            String targetID = issueLinkManager.getLinkCollection(getIssue(), getRemoteUser()).getAllIssues().isEmpty() ? "" : "#linkingmodule";

            if (isInlineDialogMode())
            {
                return returnCompleteWithInlineRedirect("/browse/" + getIssue().getString("key") + targetID);
            }

            return returnComplete("/browse/" + getIssueObject().getKey() + targetID);

        }
        else
        {
            // No confirmation supplied - ask for one
            return INPUT;
        }
    }

    public String getDirectionName() throws GenericEntityException
    {
        if (destId != null)
        { return getLink().getIssueLinkType().getOutward(); }
        else if (sourceId != null)
        { return getLink().getIssueLinkType().getInward(); }

        return null;
    }

    public String getTargetIssueKey()
    {
        Issue issue = getIssueManager().getIssueObject(destId != null ? destId : sourceId);
        if (issue != null)
        { return issue.getKey(); }

        return null;
    }

    public Long getDestId()
    {
        return destId;
    }

    public void setDestId(Long destId)
    {
        this.destId = destId;
    }

    public Long getSourceId()
    {
        return sourceId;
    }

    public void setSourceId(Long sourceId)
    {
        this.sourceId = sourceId;
    }

    public Long getLinkType()
    {
        return linkType;
    }

    public void setLinkType(Long linkType)
    {
        this.linkType = linkType;
    }

    public boolean isConfirm()
    {
        return confirm;
    }

    public void setConfirm(boolean confirm)
    {
        this.confirm = confirm;
    }

    private IssueLink getLink() throws GenericEntityException
    {
        if (issueLink == null)
        {
            if (destId != null)
            { issueLink = issueLinkManager.getIssueLink(getId(), destId, linkType); }
            else if (sourceId != null)
            { issueLink = issueLinkManager.getIssueLink(sourceId, getId(), linkType); }
        }

        return issueLink;
    }
}
