package com.atlassian.renderer.util;

import com.opensymphony.util.TextUtils;
import org.apache.log4j.Category;

import java.util.StringTokenizer;

/**
 * Some simple methods that the renderer uses.
 */
public class RendererUtil
{
    private static final Category log = Category.getInstance(RendererUtil.class);

    /**
     * Strips basic wiki markup from the content passed in
     * and truncates string to 50 chars.
     *
     * @param content wiki marked up content
     * @return first 255 chars of the stripped content
     */
    public static String summarise(String content)
    {
        if (!TextUtils.stringSet(content))
            return content;

        content = stripBasicMarkup(content);

        int urlIdx = content.indexOf("http://");
        if (urlIdx > 0)
            content = content.substring(0, urlIdx);

        return summariseWithoutStrippingWikiCharacters(content).trim();
    }

    /**
     * Strip basic markup characters from a piece of content
     */
    public static String stripBasicMarkup(String content)
    {
        if (!TextUtils.stringSet(content))
            return content;

        content = content.replaceAll("h[0-9]\\.", " "); // headings
        content = content.replaceAll("\\[.*///.*\\]", ""); // system links
        content = content.replaceAll("[\\[\\]\\*_\\^\\-\\~\\+]", ""); // basic formatting
        content = content.replaceAll("\\|", " "); // table breaks
        content = content.replaceAll("\\{([^:\\}\\{]+)(?::([^\\}\\{]*))?\\}(?!\\})", " "); // macros
        content = content.replaceAll("\\n", " ");
        content = content.replaceAll("\\r", " ");
        content = content.replaceAll("bq\\.", " ");
        content = content.replaceAll("  ", " ");

        return content;
    }

    public static String summariseWithoutStrippingWikiCharacters(String content)
    {
        if (!TextUtils.stringSet(content))
            return content;

        StringTokenizer st = new StringTokenizer(content, " ");

        if (st.countTokens() == 1)  //if it's but one word, exceeding 50 char.s,
        {                           //the length should be 46, plus a trailing space and ellipse.
            if (content != null && content.length() > 50)
            {
                content = content.substring(1, 46);
                content = content.concat(" ...");
            }
        }
        else if (content != null && content.length() > 255)
        {
            content =  TextUtils.trimToEndingChar(content, 251) + "...";
        }

        return content;
    }
}
