/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 9:18:05 AM
 */
package com.atlassian.renderer.v2.components.table;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.RenderMode;
import com.atlassian.renderer.v2.RenderUtils;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

public class TableCell implements Renderable
{
    private final String content;
    private boolean header;

    public TableCell(String content)
    {
        this(content, false);
    }

    public TableCell(String content, boolean header)
    {
        this.content = content;
        this.header = header;
    }

    public String getContent()
    {
        return content;
    }

    public boolean isHeader()
    {
        return header;
    }

    public void render(SubRenderer subRenderer, RenderContext context, StringBuffer buff)
    {
        String renderedContent = subRenderer.render(content, context, RenderMode.TABLE_CELL);
        if (RenderUtils.isBlank(renderedContent))
            renderedContent = "&nbsp;";

        if (isHeader())
            buff.append("<th class='confluenceTh'>").append(renderedContent).append("</th>");
        else
            buff.append("<td class='confluenceTd'>").append(renderedContent).append("</td>");

        buff.append("\n");
    }

    public String toString()
    {
        return "|" + (header ? "|" : "") + content;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TableCell)) return false;

        final TableCell tableCell = (TableCell) o;

        if (header != tableCell.header) return false;
        if (content != null ? !content.equals(tableCell.content) : tableCell.content != null) return false;

        return true;
    }

    public int hashCode()
    {
        int result;
        result = (content != null ? content.hashCode() : 0);
        result = 29 * result + (header ? 1 : 0);
        return result;
    }

}