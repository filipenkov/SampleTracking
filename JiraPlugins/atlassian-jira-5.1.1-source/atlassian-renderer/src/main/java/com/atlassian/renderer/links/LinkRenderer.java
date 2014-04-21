package com.atlassian.renderer.links;

import com.atlassian.renderer.RenderContext;

/**
 * Implemented by the renderer to convert a Link object into something that makes sense in the
 * rendered context.
 */
public interface LinkRenderer
{
    /**
     * Render a link as HTML, given a current PageContext.
     *
     * @param link   the link to append
     * @param context the current rendering context
     * @return The HTML result of this link.
     */
    String renderLink(Link link, RenderContext context);
}
