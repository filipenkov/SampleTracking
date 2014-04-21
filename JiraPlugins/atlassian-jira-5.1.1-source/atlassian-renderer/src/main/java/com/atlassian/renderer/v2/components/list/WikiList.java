package com.atlassian.renderer.v2.components.list;

import com.atlassian.renderer.v2.SubRenderer;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.RenderContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

class WikiList
{
    public final ListType type;
    private final List items = new ArrayList();
    private ListItem lastItem;

    public WikiList(ListType type)
    {
        this.type = type;
    }

    public void addListItem(String bullets, ListItem item)
    {
        if (bullets.length() == 1)
            addItem(item);
        else
        {
            if (lastItem == null)
                addItem(new ListItem(""));

            lastItem.addListItem(bullets.substring(1), item);
        }
    }

    private void addItem(ListItem item)
    {
        lastItem = item;
        items.add(item);
    }

    public void toHtml(StringBuffer buffer, int depth, SubRenderer subRenderer, RenderContext context)
    {
        RenderUtils.tabTo(buffer, depth);
        buffer.append(type.openingTag).append("\n");

        for (Iterator it = items.iterator(); it.hasNext();)
        {
            ListItem listItem = (ListItem) it.next();
            listItem.toHtml(buffer, depth + 1, subRenderer, context);
        }

        RenderUtils.tabTo(buffer, depth);
        buffer.append(type.closingTag).append("\n");
    }
}
