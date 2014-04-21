package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;

public interface Renderer
{
    /**
     * Render some content without changing render modes.
     *
     * @param originalContent the content to render
     * @param renderContext the context in which it is being rendered
     * @return the resulting HTML
     */
    String render(String originalContent, RenderContext renderContext);

    /**
     * Render some content as text, performing any transforms that may be needed to produce
     * nicely formatted text.
     *
     * @param originalContent is the content to render.
     * @param context
     * @return a string that represents the text of the original content which has
     * been stripped of any markup characters that need not be indexed by lucene.
     */
    public String renderAsText(String originalContent, RenderContext context);

    /**
     * Returns a unique type that describes the renderer.
     * @return a string that uniquely identifies the renderer.
     */
    public String getRendererType();
}
