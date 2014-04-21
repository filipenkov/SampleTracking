package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.components.link.LinkDecorator;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Escapes certain symbols prefixed with \
 */
public class BackslashEscapeRendererComponent extends AbstractRegexRendererComponent
{

    private static final char[] ESCAPABLE_WIKI_CHARS = { '!', '[', ']', '^', '~', '+', '?', '%', '{', '}', '(', ')', '*', '_', '-', '|', '@' };

    public static String escapeWiki(String str)
    {
        StringBuffer buf = new StringBuffer(str.length() + 10);

        for (int i = 0; i < str.length(); i++)
        {
            char c = str.charAt(i);

            for (int j = 0; j < ESCAPABLE_WIKI_CHARS.length; j++)
            {
                char escapableWikiChar = ESCAPABLE_WIKI_CHARS[j];
                if (c == escapableWikiChar)
                    buf.append('\\');
            }

            buf.append(c);
        }

        return buf.toString();
    }

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.backslashEscape();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("\\") == -1)
        {
            return wiki;
        }
        return regexRender(wiki, context, Pattern.compile("(^|(?<!\\\\))\\\\([\\-\\#\\*\\_\\+\\?\\^\\~\\|\\%\\{\\}\\[\\]\\(\\)\\!\\@])"));
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        char c = matcher.group(2).charAt(0);

        // Because the escape filter runs after the macro and link filters, those filters have to take care of
        // their own escaping, but we
        if (c == '{' || c == '}' || c == '[' || c == ']')
        {
            if (context.isRenderingForWysiwyg() && (c == '{' || c == '}'))
            {
                buffer.append(context.getRenderedContentStore().addInline("\\" + c));
            }
            else
            {
                buffer.append(c);
            }
        }
        else
            buffer.append("&#").append(((int)c)).append(";");
    }
}
