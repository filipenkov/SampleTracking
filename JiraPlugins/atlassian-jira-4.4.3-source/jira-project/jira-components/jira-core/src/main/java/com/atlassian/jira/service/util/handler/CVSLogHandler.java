/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.mail.MailThreadManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.mail.MailUtils;
import com.opensymphony.user.User;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

public class CVSLogHandler extends AbstractMessageHandler
{
    private static final Logger log = Logger.getLogger(CVSLogHandler.class);
    private static final String COMMENT_START_LINE = "Log Message:";
    private static final String COMMENT_END_LINE_1 = "Index:";
    private static final String COMMENT_END_LINE_2 = "===================================================================";
    private static final String COMMENT_2_END_LINE = "--- NEW FILE:";
    private static final String COMMENT_3_END_LINE = "DELETED ---";

    public boolean handleMessage(Message message) throws MessagingException
    {
        if (!canHandleMessage(message))
        {
            return deleteEmail;
        }

        final String messageBody = MailUtils.getBody(message);
        final String commentArea = getCommentArea(messageBody);

        //get either the sender of the message, or the default reporter
        User reporter = getReporter(message);

        //no reporter - so reject the message
        if (reporter == null)
        {
            log.warn("The mail 'FROM' does not match a valid user");
            log.warn("This user is not in jira so can not add a comment: " + message.getFrom()[0]);
            return false; // Don't delete an email if we don't deal with it.
        }

        GenericValue[] issues = ServiceUtils.findIssuesInString(commentArea);
        if (issues != null && issues.length > 0)
        {
            final CommentManager commentManager = getCommentManager();
            final IssueFactory issueFactory = (IssueFactory) ComponentManager.getComponentInstanceOfType(IssueFactory.class);

            for (int i = 0; i < issues.length; i++)
            {
                try
                {
                    GenericValue issueGV = issues[i];
                    if (hasUserPermissionToComment(issueGV, reporter))
                    {
                        final String commentBody = createCommentBody(message.getSubject(), commentArea);
                        final MutableIssue issue = issueFactory.getIssue(issueGV);
                        commentManager.create(issue, reporter.getName(), commentBody, null, null, true);
                    }
                    else
                    {
                        log.warn("You (" + reporter.getDisplayName() + ") do not have permission to comment on an issue in project: " + issueGV.getLong("project"));
                    }

                    // Record the message id of this e-mail message so we can track replies to this message
                    // and associate them with this issue
                    recordMessageId(MailThreadManager.ISSUE_COMMENTED_FROM_EMAIL, message, issueGV.getLong("id"));
                }
                catch (RuntimeException e)
                {
                    log.warn("Exception creating comment " + e.getMessage(), e);
                }
            }
        }
        return true; //delete message
    }

    /**
     * Creates and returns a new comment body.
     *
     * @param subject     message subject, can be null
     * @param commentArea comment area extracted previously from the message
     * @return comment body
     */
    private String createCommentBody(String subject, String commentArea)
    {
        StringBuffer commentBody = new StringBuffer(32);
        commentBody.append("CVS COMMIT LOG: \n");
        if (subject != null)
        {
            commentBody.append("SUBJECT: ").append(subject).append("\n");
        }
        commentBody.append(commentArea);
        return commentBody.toString();
    }

    /**
     * This method was extracted and made protected to allow unit test mocking
     *
     * @return comment manager
     */
    protected CommentManager getCommentManager()
    {
        return (CommentManager) ComponentManager.getComponentInstanceOfType(CommentManager.class);
    }

    /**
     * Returns true if the specified user has permission to comment on given issue.
     *
     * @param issue    issue to comment on
     * @param reporter commenting user
     * @return true if permission granted, false otherwise
     */
    protected boolean hasUserPermissionToComment(GenericValue issue, User reporter)
    {
        return ManagerFactory.getPermissionManager().hasPermission(Permissions.COMMENT_ISSUE, issue, reporter);
    }

    /**
     * Get the actual comment text from a CVS log email.  This is done by reading through line
     * by line, and parsing the comment body.
     *
     * @param messageBody message body
     * @return comment from CVS log email
     */
    public String getCommentArea(String messageBody)
    {
        if (messageBody == null)
        {
            return null;
        }

        BufferedReader reader = new BufferedReader(new StringReader(messageBody));
        boolean insideComment = false;
        StringBuffer comment = new StringBuffer();

        try
        {
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                //if we are not parsing a comment yet - we should look to start one
                if (!insideComment)
                {
                    //if we find the start line - set the state to be inside comment
                    if (line.toLowerCase().startsWith(COMMENT_START_LINE.toLowerCase()))
                    {
                        insideComment = true;
                    }
                }
                else
                {
                    //if we are inside a comment - look for the end line
                    //check for the one-line end first
                    if (line.startsWith(COMMENT_2_END_LINE) || line.endsWith(COMMENT_3_END_LINE) || line.endsWith(COMMENT_END_LINE_2))
                    {
                        insideComment = false;
                        return comment.toString();
                    }
                    else if (line.startsWith(COMMENT_END_LINE_1))
                    {
                        //check for second line
                        String line2;
                        if ((line2 = reader.readLine()) != null)
                        {
                            if (line2.startsWith(COMMENT_END_LINE_2))
                            {
                                insideComment = false;
                                return comment.toString();
                            }
                            else
                            {
                                comment.append(line);
                                comment.append("\n");
                                comment.append(line2);
                                comment.append("\n");
                            }
                        }
                    }
                    else
                    {
                        //else keep adding more content to the comment
                        comment.append(line);
                        comment.append("\n");
                    }
                }
            }
        }
        catch (IOException e)
        {
            log.warn("IOException reading Mail body: " + e.getMessage(), e);
        }

        //if still inside comment when reach the end of the email - do NOT add it
        if (insideComment)
        {
            log.warn("UnClosed Comment Tag!  Not adding comment: [" + comment.substring(0, 15) + "]");
        }

        return null;
    }

    /**
     * Plain text parts are currently not attached, and simply ignored and lost.
     *
     * This method always returns false.
     *
     * @param part
     * @return
     */
    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return false;
    }

    /**
     * Html parts are currently not attached, and simply ignored and lost.
     *
     * This method always returns false.
     *
     * @param part
     * @return
     * @throws MessagingException
     * @throws IOException
     */
    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return false;
    }
}
