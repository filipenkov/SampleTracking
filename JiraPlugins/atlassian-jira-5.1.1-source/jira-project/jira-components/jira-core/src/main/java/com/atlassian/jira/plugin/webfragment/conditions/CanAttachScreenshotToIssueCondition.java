package com.atlassian.jira.plugin.webfragment.conditions;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.issue.attachment.AttachmentService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.log4j.Logger;

/**
 * Condition that determines whether the current user can attach a screenshot to the current issue.
 * <p/>
 * An issue must be in the JiraHelper context params.
 *
 * @since v4.1
 */
public class CanAttachScreenshotToIssueCondition extends AbstractIssueCondition
{
    private static final Logger log = Logger.getLogger(CanAttachScreenshotToIssueCondition.class);
    private final AttachmentService attachmentService;


    public CanAttachScreenshotToIssueCondition(AttachmentService attachmentService)
    {
        this.attachmentService = attachmentService;
    }

    public boolean shouldDisplay(User user, Issue issue, JiraHelper jiraHelper)
    {
        JiraServiceContext context = new JiraServiceContextImpl(user, new SimpleErrorCollection());
        return attachmentService.canAttachScreenshots(context, issue);
    }

}