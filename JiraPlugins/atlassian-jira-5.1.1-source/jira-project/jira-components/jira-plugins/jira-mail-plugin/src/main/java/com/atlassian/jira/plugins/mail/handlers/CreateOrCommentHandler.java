/*
 * Copyright (c) 2002-2006
 * All rights reserved.
 */

package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.service.util.ServiceUtils;
import com.atlassian.jira.service.util.handler.MessageHandlerContext;
import com.atlassian.jira.service.util.handler.MessageHandlerErrorCollector;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.mail.MailUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.util.Map;

/**
 * A message handler to create a new issue, or add a comment
 * to an existing issue, from an incoming message. If the subject
 * contains a project key the message is added as a comment to that
 * issue. If no project key is found, a new issue is created in the
 * default project.
 */
public class CreateOrCommentHandler extends AbstractMessageHandler
{
    /**
     * Default project where new issues are created.
     */
    public String projectKey;

    /**
     * Default type for new issues.
     */
    public String issueType;

    /**
     * If set (to anything except "false"), quoted text is removed from comments.
     */
    public String stripquotes;
    public static final String KEY_PROJECT = "project";
    public static final String KEY_ISSUETYPE = "issuetype";
    public static final String KEY_QUOTES = "stripquotes";
    private static final String FALSE = "false";

    public boolean handleMessage(Message message, MessageHandlerContext context)
            throws MessagingException
    {
        String subject = message.getSubject();

        if (!canHandleMessage(message, context.getMonitor()))
        {
            if (log.isDebugEnabled())
            {
                log.debug("Cannot handle message '" + subject + "'.");
            }
            return deleteEmail;
        }

        if (log.isDebugEnabled())
        {
            log.debug("Looking for Issue Key in subject '" + subject + "'.");
        }
        Issue issue = ServiceUtils.findIssueObjectInString(subject);

        if (issue == null)
        {
            // If we cannot find the issue from the subject of the e-mail message
            // try finding the issue using the in-reply-to message id of the e-mail message
            log.debug("Issue Key not found in subject '" + subject + "'. Inspecting the in-reply-to message ID.");
            issue = getAssociatedIssue(message);
        }

        // if we have found an associated issue
        if (issue != null)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Issue '" + issue.getKey() + "' found for email '" + subject + "'.");
            }
            boolean doDelete;

            //add the message as a comment to the issue
            if ((stripquotes == null) || FALSE.equalsIgnoreCase(stripquotes)) //if stripquotes not defined in setup
            {
                FullCommentHandler fc = new FullCommentHandler()
                {
                    @Override
                    protected MessageUserProcessor getMessageUserProcessor()
                    {
                        return CreateOrCommentHandler.this.getMessageUserProcessor();
                    }
                };
                fc.init(params, context.getMonitor());
                doDelete = fc.handleMessage(message, context); //get message with quotes
            }
            else
            {
                NonQuotedCommentHandler nq = new NonQuotedCommentHandler()
                {
                    @Override
                    protected MessageUserProcessor getMessageUserProcessor()
                    {
                        return CreateOrCommentHandler.this.getMessageUserProcessor();
                    }
                };

                nq.init(params, context.getMonitor());
                doDelete = nq.handleMessage(message, context); //get message without quotes
            }
            return doDelete;
        }
        else
        { //no issue found, so create new issue in default project
            if (log.isDebugEnabled())
            {
                log.debug("No Issue found for email '" + subject + "' - creating a new Issue.");
            }

            CreateIssueHandler createIssueHandler = new CreateIssueHandler()
            {
                @Override
                protected MessageUserProcessor getMessageUserProcessor()
                {
                    return CreateOrCommentHandler.this.getMessageUserProcessor();
                }
            };

            createIssueHandler.init(params, context.getMonitor());
            return createIssueHandler.handleMessage(message, context);
        }
    }

    public void init(Map<String, String> params, MessageHandlerErrorCollector errorCollector)
    {
        log.debug("CreateOrCommentHandler.init(params: " + params + ")");

        super.init(params, errorCollector);

        if (params.containsKey(KEY_PROJECT))
        {
            projectKey = params.get(KEY_PROJECT);
        }

        if (params.containsKey(KEY_ISSUETYPE))
        {
            issueType = params.get(KEY_ISSUETYPE);
        }

        if (params.containsKey(KEY_QUOTES))
        {
            stripquotes = params.get(KEY_QUOTES);
        }
    }

    /**
     * Plain text parts must be kept if they are not empty.
     *
     * @param part The plain text part being tested.
     * @return Returns true to attach false otherwise.
     */
    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part);
    }

    /**
     * Comments never wish to keep html parts that are not attachments as they extract the plain text
     * part and use that as the content. This method therefore is hard wired to always return false.
     *
     * @param part The html part being processed
     * @return Always return false.
     * @throws MessagingException
     * @throws IOException
     */
    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return false;
    }
}
