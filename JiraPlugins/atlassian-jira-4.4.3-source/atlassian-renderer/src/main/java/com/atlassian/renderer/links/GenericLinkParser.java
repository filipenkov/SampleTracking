package com.atlassian.renderer.links;

import com.opensymphony.util.TextUtils;

import java.text.ParseException;

public class GenericLinkParser
{
    private String originalLinkText;
    private String linkBody;
    private String notLinkBody;
    private String linkTitle;
    private String spaceKey;
    private String destinationTitle = "";
    private String anchor;
    private String shortcutName;
    private String shortcutValue;
    private String attachmentName;
    private long contentId;

    public GenericLinkParser(String linkText)
    {
        this.originalLinkText = linkText;
        // we want to decode single quotes (represented by &#039;) back before parsing the link test
        if (linkText.indexOf("&#039;") != -1)
        {
            linkText = linkText.replaceAll("&#039;", "\'");
        }
        StringBuffer buf = new StringBuffer(linkText);
        linkBody = extractLinkBody(buf);
        linkTitle = trimIfPossible(divideAfter(buf, '|'));
        notLinkBody = buf.toString().trim();
    }

    public void parseAsContentLink() throws ParseException
    {
        // Don't treat it as a short link when it starts with "~"
        if (!notLinkBody.startsWith("~"))
        {
            StringBuffer shortcutBuf = new StringBuffer(notLinkBody);
            shortcutName = trimIfPossible(divideAfterLast(shortcutBuf, '@'));
            shortcutValue = shortcutBuf.toString();
        }

        StringBuffer buf = new StringBuffer(notLinkBody);

        if (!TextUtils.stringSet(shortcutName))
        {
            spaceKey = trimIfPossible(divideOn(buf, ':'));

            if (buf.indexOf("$") == 0)
            {
                buf.deleteCharAt(0);
                contentId = extractNumber(buf);
                if (contentId == 0)
                    return;
            }

            attachmentName = trimIfPossible(divideAfter(buf, '^'));
            anchor = trimIfPossible(divideAfter(buf, '#'));
        }

        if (contentId == 0)
            destinationTitle = buf.toString().trim();
    }

    private long extractNumber(StringBuffer buf)
    {
        StringBuffer digits = new StringBuffer(10);
        int i = 0;
        for (; i < buf.length() && Character.isDigit(buf.charAt(i)); i++)
        {
            digits.append(buf.charAt(i));
        }

        if (i > 0)
            buf.delete(0, i);

        try
        {
            return Long.parseLong(digits.toString());
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    private String trimIfPossible(String s)
    {
        if (s == null)
            return null;

        return s.trim();
    }

    public String getOriginalLinkText()
    {
        return originalLinkText;
    }

    public String getLinkBody()
    {
        return linkBody;
    }

    public String getNotLinkBody()
    {
        return notLinkBody;
    }

    public String getSpaceKey()
    {
        return spaceKey;
    }

    public String getDestinationTitle()
    {
        return destinationTitle;
    }

    public String getAnchor()
    {
        return anchor;
    }

    public String getShortcutName()
    {
        return shortcutName;
    }

    public String getShortcutValue()
    {
        return shortcutValue;
    }

    public String getLinkTitle()
    {
        return linkTitle;
    }

    public String getAttachmentName()
    {
        return attachmentName;
    }

    public long getContentId()
    {
        return contentId;
    }

    private String extractLinkBody(StringBuffer buffer)
    {
        if (buffer.indexOf("!") == -1 || buffer.indexOf("!") > buffer.indexOf("|") || buffer.indexOf("!") == buffer.lastIndexOf("!"))
            return divideOn(buffer, '|');
        else
        {
            StringBuffer body = new StringBuffer();
            boolean inEscape = false;

            for (int i = 0; i < buffer.length(); i++)
            {
                char c = buffer.charAt(i);
                if (c == '!')
                    inEscape = !inEscape;
                if (c == '|' && !inEscape)
                {
                    buffer.delete(0, i + 1);
                    return body.toString();
                }
                body.append(c);
            }

            return null;
        }

    }

    /**
     * Split a StringBuffer on some dividing character. Return everything before the divider,
     * and remove that prefix _and_ the divider from the StringBuffer. If there is no divider,
     * return null.
     * <p/>
     * If the buffer begins with the divider, then the divider will be removed _and_ null returned.
     * If the buffer ends with the divider, everything before the divider is returned and the buffer
     * will remain empty.
     *
     * @param buffer  the text we want to divide. Will be modified during the operation
     * @param divider the character to divide the buffer on
     * @return the characters before the divider, or the default if there are none
     */
    public static String divideOn(StringBuffer buffer, char divider)
    {
        if (buffer.length() == 0)
            return null;

        int i = buffer.indexOf(Character.toString(divider));

        if (i < 0)
        {
            return null;
        }
        else if (i == 0)
        {
            buffer.deleteCharAt(0);
            return null;
        }
        else
        {
            String body = buffer.substring(0, i);
            buffer.delete(0, i + 1);
            return body;
        }
    }

    private String divideAfter(StringBuffer buffer, char divider)
    {
        if (buffer.length() == 0)
            return null;

        return divideAfter(buffer, buffer.indexOf(Character.toString(divider)));
    }

    private String divideAfterLast(StringBuffer buffer, char divider)
    {
        if (buffer.length() == 0)
            return null;

        return divideAfter(buffer, buffer.lastIndexOf(Character.toString(divider)));
    }

    private String divideAfter(StringBuffer buffer, int index)
    {
        if (index < 0)
        {
            return null;
        }
        else if (index == buffer.length() - 1)
        {
            buffer.deleteCharAt(buffer.length() - 1);
            return null;
        }
        else
        {
            String body = buffer.substring(index + 1);
            buffer.delete(index, buffer.length());
            return body;
        }

    }
}
