package com.atlassian.jira.service.util.handler;

import com.atlassian.mail.MailUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.oro.text.perl.MalformedPerl5PatternException;
import org.apache.oro.text.perl.Perl5Util;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RegexCommentHandler extends AbstractCommentHandler
{
    private static final Logger log = Logger.getLogger(RegexCommentHandler.class);

    private static final String KEY_SPLITREGEX = "splitregex";
    private String splitRegex;

    public void init(Map params)
    {
        super.init(params);
        if (params.containsKey(KEY_SPLITREGEX))
        {
            setSplitRegex((String) params.get(KEY_SPLITREGEX));
        }
    }

    protected String getEmailBody(Message message) throws MessagingException
    {
        return splitMailBody(MailUtils.getBody(message));
    }

    public String splitMailBody(String rawBody)
    {
        final String splitRegex = getSplitRegex();
        try
        {
            if (StringUtils.isNotEmpty(splitRegex))
            {
                final List parts = new ArrayList();
                new Perl5Util().split(parts, splitRegex, rawBody);
                if (parts.isEmpty())
                {
                    log.debug("Regex " + splitRegex + " did not match any text in email; using full text for comment.");
                }
                else if (parts.size() > 1)
                {
                    log.debug("Regex " + splitRegex + " matched " + parts.size() + " times; using first as comment.");
                    StringBuffer comment = new StringBuffer("\n");
                    comment.append(((String) parts.get(0)).trim());
                    comment.append("\n\n");
                    return comment.toString();
                }
            }
        }
        catch (MalformedPerl5PatternException pe)
        {
            log.error("Invalid regex in parameter " + KEY_SPLITREGEX + "=" + splitRegex + " on regex comment handler. Note that regex must be in the format /foo/, and cannot contain commas (as they are used for separating handler params). " + pe, pe);
        }
        catch (RuntimeException e)
        {
            log.warn("Failed to split email body. Appending raw content...", e);
        }
        return rawBody;
    }

    public String getSplitRegex()
    {
        return splitRegex;
    }

    public void setSplitRegex(String splitRegex)
    {
        this.splitRegex = splitRegex;
    }

    /**
     * Plain text parts must be kept if they arent empty.
     *
     * @param part
     * @return
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
     * @return Always returns false
     * @throws MessagingException
     * @throws IOException
     */
    protected boolean attachHtmlParts(final Part part) throws MessagingException, IOException
    {
        return false;
    }
}
