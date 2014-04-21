package com.atlassian.jira.web.action.issue;

import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.issue.attachment.Attachment;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;

/**
 */
public class DeleteAttachment extends AbstractIssueSelectAction
{
    private final AttachmentService attachmentService;
    private Long deleteAttachmentId;

    public DeleteAttachment(AttachmentService attachmentService)
    {
        this.attachmentService = attachmentService;
    }

    public void doValidation()
    {
        attachmentService.canDeleteAttachment(getJiraServiceContext(), deleteAttachmentId);
    }

    @RequiresXsrfCheck
    public String doExecute() throws Exception
    {
        attachmentService.delete(getJiraServiceContext(), deleteAttachmentId);
        if (hasAnyErrors())
        {
            return ERROR;
        }
        return getRedirect("ManageAttachments.jspa?id=" + getIssueId());
    }

    public Long getDeleteAttachmentId()
    {
        return deleteAttachmentId;
    }

    public void setDeleteAttachmentId(Long deleteAttachmentId)
    {
        this.deleteAttachmentId = deleteAttachmentId;
    }

    public Long getIssueId()
    {
        return getIssueObject().getId();
    }

    public Attachment getAttachment()
    {
        return attachmentService.getAttachment(getJiraServiceContext(), deleteAttachmentId);
    }
}