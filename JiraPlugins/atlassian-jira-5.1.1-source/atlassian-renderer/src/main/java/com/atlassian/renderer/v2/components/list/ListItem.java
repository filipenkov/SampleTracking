package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.RenderContext;
import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

class ListItem
{
    private final String contents;
    private final List children = new ArrayList();
    private WikiList lastChild;

    public ListItem(String contents)
    {
        this.contents = contents;
    }

    public void toHtml(StringBuffer buffer, int depth, SubRenderer subRenderer, RenderContext context)
    {
        for (int i = 0; i < depth; i++)
            buffer.append("\t");

        buffer.append("<li>");
        if (TextUtils.stringSet(contents))
            buffer.append(subRenderer.render(contents, context, RenderMode.LIST_ITEM));

        if (lastChild != null)
        {
            buffer.append("\n");
            appendChildren(buffer, depth, subRenderer, context);
            RenderUtils.tabTo(buffer, depth);
        }

        buffer.append("</li>\n");
    }

    protected void appendChildren(StringBuffer buffer, int depth, SubRenderer subRenderer, RenderContext context)
    {
        for (Iterator it = children.iterator(); it.hasNext();)
        {
            WikiList wikiList = (WikiList) it.next();
            wikiList.toHtml(buffer, depth, subRenderer, context);
        }
    }

    public void addListItem(String bullets, ListItem item)
    {
        if (lastChild == null || isSingleNonMatchingBullet(bullets))
        {
            addList(bullets.substring(0, 1));
        }

        lastChild.addListItem(bullets, item);
    }

    private boolean isSingleNonMatchingBullet(String bullets)
    {
        return bullets.length() == 1 && !lastChild.type.bullet.equals(bullets);
    }

    private void addList(String bullet)
    {
        WikiList list = new WikiList((ListType) ListBlockRenderer.LIST_TYPES.get(bullet));
        lastChild = list;
        children.add(list);

    }
}
