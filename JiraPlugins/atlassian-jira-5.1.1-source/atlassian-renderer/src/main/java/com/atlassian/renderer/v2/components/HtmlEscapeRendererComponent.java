package com.atlassian.renderer.v2.components;

import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.RenderContext;

public class HtmlEscapeRendererComponent implements RendererComponent
{

    public boolean shouldRender(RenderMode renderMode)
    {
        return renderMode.htmlEscape();
    }

    public String render(String wiki, RenderContext context)
    {
        return HtmlEscaper.escapeAllExceptQuotes(wiki, context.getRenderMode().preserveEntities());
    }

    /**
     * Escapes HTML
     *
     * @deprecated since 3.12 see {@link HtmlEscaper#escapeAllExceptQuotes(String, boolean)}
     * @see HtmlEscaper
     */
    public static String escapeHtml(String s, boolean preserveExistingEntities)
    {
        return HtmlEscaper.escapeAllExceptQuotes(s, preserveExistingEntities);
    }

}
