package com.atlassian.renderer.v2.components.phrase;

import com.atlassian.renderer.v2.components.AbstractRegexRendererComponent;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ForceNewLineRendererComponent extends AbstractRegexRendererComponent
{
    private static final Pattern FORCE_NEWLINE = Pattern.compile("(?<!\\\\)\\\\{2}(?!\\S*\\\\)");

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.renderLinebreaks();
    }

    public String render(String wiki, RenderContext context)
    {
        if (wiki.indexOf("\\\\") == -1)
        {
            return wiki;
        }
        return regexRender(wiki, context, FORCE_NEWLINE);
    }

    public void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher)
    {
        buffer.append("<br clear=\"all\" />");
    }
}
