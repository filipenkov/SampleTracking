/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 29, 2004
 * Time: 9:45:36 AM
 */
package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.components.HtmlEscaper;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

public class RenderUtils
{
    public static final String WIKI_SRC_CLASS = "wikisrc";
    private static final Pattern CONTROL_CHAR_PATTERN = Pattern.compile("\u0001|\u0002");

    public static boolean isBlank(String str)
    {
        //return !NON_WHITESPACE.matcher(str).find();

        for (int i = 0; i < str.length(); ++i)
        {
            if (!Character.isWhitespace(str.charAt(i)))
            {
                return false;
            }
        }
        return true;

    }

    public static String blockError(String message, String contents)
    {
        return "<div class=\"error\"><span class=\"error\">" + HtmlEscaper.escapeAll(message, true) + "</span> " +
            contents + "</div>";
    }

    //TODO: tom to decide the most appropriate way to handle errors within the wysiwyg environment.
    public static String error(RenderContext context, String message, String wysiwygContents, boolean suppressMessageOnWysiwyg)
    {
        String wysiwyg = "";
        if (context.isRenderingForWysiwyg() && wysiwygContents != null)
        {
            wysiwyg = " <span class=\"" + WIKI_SRC_CLASS + "\">" + wysiwygContents + "</span>";
            if (suppressMessageOnWysiwyg)
            {
                message = "";
            }
        }
        return "<span class=\"error\">" + HtmlEscaper.escapeAll(message, true) + "</span>" + wysiwyg;
    }

    public static String error(String message)
    {
        return "<span class=\"error\">" + HtmlEscaper.escapeAll(message, true) + "</span>";
    }

    public static void tabTo(StringBuffer buf, int depth)
    {
        for (int i = 0; i < depth; i++)
        {
            buf.append("\t");
        }
    }

    public static String getParameter(Map parameters, String key, int alternateIndex)
    {
        String result = (String) parameters.get(key);
        if (StringUtils.isBlank(result))
        {
            result = TextUtils.noNull((String) parameters.get(Integer.toString(alternateIndex)));
        }

        return result;
    }

    /**
     * Strips control characters used by the renderer to indicate tokens during recursive processing of wiki markup. See
     * {@link com.atlassian.renderer.util.RenderedContentToken} for where these control characters are used. Does not
     * expect content to be null
     *
     * @since v3.14.4
     *
     * @param content content to be stripped of control characters
     * @return sanitised content
     */
    static String stripControlCharacters(final String content)
    {
        return CONTROL_CHAR_PATTERN.matcher(content).replaceAll("");
    }
}