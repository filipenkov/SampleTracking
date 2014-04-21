package com.atlassian.jira.plugins.mail.handlers;

import com.atlassian.core.util.ClassLoaderUtils;
import com.atlassian.jira.JiraApplicationContext;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.comments.CommentManager;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.mail.MailLoggingManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.service.util.handler.MessageUserProcessor;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.mail.MailUtils;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * This handler adds the body of the email as a comment, using the subject
 * to determine which issue to add the comment to.
 * <p/>
 * The difference between this and FullCommentHandler is that this will
 * strip any quoted lines from the email (ie lines that start with > or |).
 *
 * @see FullCommentHandler
 */
public class NonQuotedCommentHandler extends AbstractCommentHandler
{
    private static final String OUTLOOK_QUOTED_FILE = "outlook-email.translations";
    private Collection messages;

    public NonQuotedCommentHandler()
    {
    }

    public NonQuotedCommentHandler(PermissionManager permissionManager, IssueUpdater issueUpdater,
            UserManager userManager, ApplicationProperties applicationProperties,
            JiraApplicationContext jiraApplicationContext, MailLoggingManager mailLoggingManager,
            MessageUserProcessor messageUserProcessor)
    {
        super(permissionManager, issueUpdater, userManager, applicationProperties, jiraApplicationContext, mailLoggingManager, messageUserProcessor);
    }

    /**
     * Given a message - this handler will add the entire message body as a comment to
     * the first issue referenced in the subject.
     */
    protected String getEmailBody(Message message) throws MessagingException
    {
        return stripQuotedLines(MailUtils.getBody(message));
    }

    /**
     * Given an email body, strips quoted lines and the 'attribution' line that most mailers
     * prepend (eg. "On Wed 21 Oct 2004, Joe Bloggs wrote:").
     *
     * @param body email body
     * @return stripped email body
     */
    public String stripQuotedLines(String body)
    {
        if (body == null)
        {
            return null;
        }

        final StringTokenizer st = new StringTokenizer(body, "\n", true);
        final StringBuilder result = new StringBuilder();

        boolean strippedAttribution = false; // set to true once the attribution has been encountered
        boolean outlookQuotedLine = false; // set to true if the Microsoft Outlook reply message ("----- Original Message -----") is encountered.

        String line1;
        String line2 = null;
        String line3 = null;
        // Three-line lookahead; on each iteration, line1 may be added unless line2+line3 indicate it is an attribution
        do
        {
            line1 = line2;
            line2 = line3;
            line3 = (st.hasMoreTokens() ? st.nextToken() : null); // read next line
            if (!"\n".equals(line3))
            {
                // Ignore the newline ending line3, if line3 isn't a newline on its own
                if (st.hasMoreTokens())
                {
                    st.nextToken();
                }
            }
            if (!strippedAttribution)
            {
                if (!outlookQuotedLine)
                {
                    outlookQuotedLine = isOutlookQuotedLine(line1);
                }

                // Found our first quoted line; the attribution line may be line1 or line2
                if (isQuotedLine(line3))
                {
                    if (looksLikeAttribution(line1))
                    {
                        line1 = "> ";
                    }
                    else if (looksLikeAttribution(line2))
                    {
                        line2 = "> ";
                    }
                    strippedAttribution = true;
                }
            }
            if (line1 != null && !isQuotedLine(line1) && !outlookQuotedLine)
            {
                result.append(line1);
                if (!"\n".equals(line1))
                {
                    result.append("\n");
                }
            }
        }
        while (line1 != null || line2 != null || line3 != null);
        return result.toString();
    }

    private boolean looksLikeAttribution(String line)
    {
        return line != null && (line.endsWith(":") || line.endsWith(":\r"));
    }

    private boolean isQuotedLine(String line)
    {
        return line != null && (line.startsWith(">") || line.startsWith("|"));
    }

    private boolean isOutlookQuotedLine(String line)
    {
        if (line != null)
        {
            for (Iterator iterator = getOutlookQuoteSeparators().iterator(); iterator.hasNext();)
            {
                String message = (String) iterator.next();
                if (line.indexOf(message) != -1)
                {
                    return true;
                }
            }
        }

        return false;
    }

    private Collection getOutlookQuoteSeparators()
    {
        if (messages == null)
        {
            messages = new LinkedList();
            BufferedReader reader = null;
            try
            {
                // The file is assumed to be UTF-8 encoded.
                reader = new BufferedReader(new InputStreamReader(ClassLoaderUtils.getResourceAsStream(OUTLOOK_QUOTED_FILE, this.getClass()), "UTF-8"));
                String message;
                while ((message = reader.readLine()) != null)
                {
                    messages.add(message);
                }
            }
            catch (IOException e)
            {
                // no more properties
                log.error("Error occurred while reading file '" + OUTLOOK_QUOTED_FILE + "'.");
            }
            finally
            {
                try
                {
                    if (reader != null)
                    {
                        reader.close();
                    }
                }
                catch (IOException e)
                {
                    log.error("Could not close the file '" + OUTLOOK_QUOTED_FILE + "'.");
                }
            }
        }
        return messages;
    }

    /**
     * Plain text parts must be kept if they arent empty.
     *
     * @param part The part being tested.
     * @return Returns true if the part content is not empty, otherwise returns false.
     */
    protected boolean attachPlainTextParts(final Part part) throws MessagingException, IOException
    {
        return !MailUtils.isContentEmpty(part);
    }

    /**
     * Comments never wish to keep html parts that are not attachments as they extract the plain text
     * part and use that as the content. This method therefore is hard wired to always return false.
     *
     * @param part The part being tested.
     * @return Always returns false
     * @throws MessagingException
     * @throws IOException
     */
    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return false;
    }
}
