/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Oct 29, 2004
 * Time: 9:29:04 AM
 */
package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderedContentStore;
import com.atlassian.renderer.RenderContext;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public abstract class AbstractRegexRendererComponent extends AbstractRendererComponent
{
    public abstract boolean shouldRender(RenderMode renderMode);

    public abstract String render(String wiki, RenderContext context);

    protected String regexRender(String wiki, RenderContext context, Pattern pattern)
    {
        if (wiki == null || wiki.length() == 0)
            return "";
        
        Matcher matcher = pattern.matcher(wiki);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find())
        {
            matcher.appendReplacement(buffer, "");
            appendSubstitution(buffer, context, matcher);
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }

    /**
     * Implement this method to perform your actual regex substitution.
     */
    public abstract void appendSubstitution(StringBuffer buffer, RenderContext context, Matcher matcher);
}