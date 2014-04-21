package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.Internal;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.web.util.AttachmentException;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.io.File;

/**
 * This class has been made public only to allow easy unit testing by tests from other packages.
 * This is the standard implementation which dispatches calls to appropriate JIRA managers.
 *
 * @since v5.0
 */
@Internal
public class DefaultMessageHandlerContext implements MessageHandlerContext
{
    private final CommentManager commentManager;
    private final MessageHandlerExecutionMonitor monitor;
    private final IssueManager issueManager;
    private final AttachmentManager attachmentManager;


    public DefaultMessageHandlerContext(CommentManager commentManager, MessageHandlerExecutionMonitor monitor, IssueManager issueManager, AttachmentManager attachmentManager)
    {
        this.commentManager = commentManager;
        this.monitor = monitor;
        this.issueManager = issueManager;
        this.attachmentManager = attachmentManager;
    }

    @Override
    public User createUser(String username, String password, String email, String fullname, Integer userEventType)
            throws PermissionException, CreateException
    {
        final User user;
        if (userEventType == null)
        {
            user = ComponentAccessor.getUserUtil().createUserNoNotification(username, password, email, fullname);

        }
        else
        {
            user = ComponentAccessor.getUserUtil().createUserWithNotification(username, password, email, fullname, userEventType);
        }
        monitor.info("Created user '" + user.getName() + ".");
        return user;
    }

    @Override
    public Comment createComment(Issue issue, User author, String body, boolean dispatchEvent)
    {
        final Comment comment = commentManager.create(issue, author.getName(), body, dispatchEvent);
        monitor.info("Added comment '" + StringUtils.abbreviate(body, 20)
                + " 'by '" + author.getName() + "' to issue '" + issue.getKey() + "'");
        return comment;
    }

    @Override
    public Issue createIssue(@Nullable User reporter, Issue issue)  throws CreateException
    {
        final Issue issueObject = issueManager.createIssueObject(reporter, issue);
        monitor.info("Issue " + issueObject.getKey() + " created");
        return issueObject;
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
            throws AttachmentException
    {
        final ChangeItemBean changeItemBean = attachmentManager.createAttachment(file, filename, contentType, author, issue);
        monitor.info("Added attachment to issue '" + issue.getKey() + "'");
        return changeItemBean;
    }

    @Override
    public boolean isRealRun()
    {
        return true;
    }


    @Override
    public MessageHandlerExecutionMonitor getMonitor()
    {
        return monitor;
    }
}
