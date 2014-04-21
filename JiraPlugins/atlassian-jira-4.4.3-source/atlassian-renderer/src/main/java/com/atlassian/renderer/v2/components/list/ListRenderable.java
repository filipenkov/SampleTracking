package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.RenderContext;

/**
 * The renderable for drawing a list
 */
class ListRenderable extends ListItem implements Renderable
{
    public ListRenderable()
    {
        super(null);
    }

    public void toHtml(StringBuffer buffer, int depth, SubRenderer subRenderer, RenderContext context)
    {
        appendChildren(buffer, depth, subRenderer, context);
    }

    public void render(SubRenderer subRenderer, RenderContext context, StringBuffer buffer)
    {
        this.toHtml(buffer, 0, subRenderer, context);
    }
}
