/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.service.util.handler;

import com.atlassian.mail.MailUtils;
import org.apache.log4j.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;

public class FullCommentHandler extends AbstractCommentHandler
{
    private static final Logger log = Logger.getLogger(FullCommentHandler.class);

    /**
     * Given a message - this handler will add the entire message body as a comment to
     * the first issue referenced in the subject.
     */
    protected String getEmailBody(Message message) throws MessagingException
    {
        return MailUtils.getBody(message);
    }

    /**
     * Plain text parts must be kept if they arent empty.
     *
     * @param part The plain text part.
     * @return True if the part is not empty, otherwise returns false
     */
    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part);
    }

    /**
     * Comments never wish to keep html parts that are not attachments as they extract the plain text
     * part and use that as the content. This method therefore is hard wired to always return false.
     *
     * @param part The html part being processed.
     * @return Always returns false
     * @throws MessagingException
     * @throws IOException
     */
    protected boolean attachHtmlParts(final javax.mail.Part part) throws MessagingException, IOException
    {
        return false;
    }
}
