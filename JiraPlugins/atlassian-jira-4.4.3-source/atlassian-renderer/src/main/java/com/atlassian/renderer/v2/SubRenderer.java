package com.atlassian.renderer.v2;

import com.atlassian.renderer.RenderContext;

/**
 * Render some sub-section of wiki content, usually in a different mode. The subrenderer is useful because it
 * takes care of things like ensuring that the RenderContext comes back out in the same state it went in.
 */
public interface SubRenderer extends Renderer
{
    /**
     * Render some wiki content in a particular render mode. The subRenderer will take care of setting the
     * new mode on the context, and returning the context to its previous mode afterwards.
     *
     * @param wiki the content to render
     * @param renderContext the context in which it is being rendered
     * @param newRenderMode the RenderMode in which to render it, or null to leave the RenderMode unchanged
     * @return the resulting HTML
     */
    String render(String wiki, RenderContext renderContext, RenderMode newRenderMode);
}
