package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.embedded.impl.ImmutableUser;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerExecutionMonitor;
import com.atlassian.jira.web.util.AttachmentException;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.util.Date;
import java.util.Map;

/**
 * The context used in Test mode of MessageHandlers -> when the output from the run is shown back to the user.
 *
 * @since v5.0
 */
public class DryRunMessageHandlerContext implements MessageHandlerContext
{
    private final Map<String, User> dummyUsersById = Maps.newHashMap();
    private final MessageHandlerExecutionMonitor messageHandlerExecutionMonitor;

    private int numCreatedIssues;
    private int numCreatedComments;
    private int numCreatedUsers;
    private int numCreatedAttachments;

    public DryRunMessageHandlerContext(MessageHandlerExecutionMonitor messageHandlerExecutionMonitor)
    {
        this.messageHandlerExecutionMonitor = messageHandlerExecutionMonitor;
    }


    @Override
    public User createUser(String username, String password, String emailAddress, String displayName, Integer userEventType)
            throws PermissionException, CreateException
    {
        User user = dummyUsersById.get(username);
        if (user == null)
        {
            messageHandlerExecutionMonitor.info("Creating user '" + username + "'.");
            numCreatedUsers++;
            user = new ImmutableUser(-1, username, displayName, emailAddress, true);
            dummyUsersById.put(username, user);
        }
        return user;
    }

    @Override
    public boolean isRealRun()
    {
        return false;
    }

    @Override
    public Comment createComment(final Issue issue, final User author, final String body, boolean dispatchEvent)
    {
        messageHandlerExecutionMonitor.info("Adding comment '" + StringUtils.abbreviate(body, 20) + "' by user '"
                + author.getName() + "' to issue '" + issue.getKey() + "'.");
        final DateTime now = new DateTime();
        numCreatedComments++;
        return new Comment()
        {
            @Override
            public String getAuthor()
            {
                return author.getName();
            }

            @Override
            public User getAuthorUser()
            {
                return author;
            }

            @Override
            public String getAuthorFullName()
            {
                return author.getDisplayName();
            }

            @Override
            public String getBody()
            {
                return body;
            }

            @Override
            public Date getCreated()
            {
                return now.toDate();
            }

            @Override
            public String getGroupLevel()
            {
                return null;
            }

            @Override
            public Long getId()
            {
                return null;
            }

            @Override
            public Long getRoleLevelId()
            {
                return null;
            }

            @Override
            public ProjectRole getRoleLevel()
            {
                return null;
            }

            @Override
            public Issue getIssue()
            {
                return issue;
            }

            @Override
            public String getUpdateAuthor()
            {
                return author.getName();
            }

            @Override
            public User getUpdateAuthorUser()
            {
                return author;
            }

            @Override
            public String getUpdateAuthorFullName()
            {
                return author.getDisplayName();
            }

            @Override
            public Date getUpdated()
            {
                return now.toDate();
            }
        };
    }

    @Override
    public Issue createIssue(User reporter, Issue issue) throws CreateException
    {
        getMonitor().info("Creating issue with summary '" + issue.getSummary() + "' and reporter '" + issue.getReporterId() + "'");
        numCreatedIssues++;
        return issue;
    }

    @Override
    public ChangeItemBean createAttachment(File file, String filename, String contentType, User author, Issue issue)
            throws AttachmentException
    {
        getMonitor().info("Creating attachment '" + filename + "'");
        numCreatedAttachments++;
        return new ChangeItemBean(); // we could return also null here, but as this parameterless constructor is available, let's use it
    }


    @Override
    public MessageHandlerExecutionMonitor getMonitor()
    {
        return messageHandlerExecutionMonitor;
    }

    public int getNumCreatedIssues()
    {
        return numCreatedIssues;
    }

    public int getNumCreatedComments()
    {
        return numCreatedComments;
    }

    public int getNumCreatedUsers()
    {
        return numCreatedUsers;
    }

    public int getNumCreatedAttachments()
    {
        return numCreatedAttachments;
    }
}
