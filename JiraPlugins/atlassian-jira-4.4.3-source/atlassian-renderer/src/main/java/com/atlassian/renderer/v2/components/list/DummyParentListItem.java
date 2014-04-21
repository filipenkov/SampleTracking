package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.RenderContext;

/**
 * A fake list item that contains all of a list, for convenience of not having to duplicate code
 */
public class DummyParentListItem extends ListItem
{
    public DummyParentListItem()
    {
        super(null);
    }

    public void toHtml(StringBuffer buffer, int depth, SubRenderer subRenderer, RenderContext context)
    {
        appendChildren(buffer, depth, subRenderer, context);
    }
}
