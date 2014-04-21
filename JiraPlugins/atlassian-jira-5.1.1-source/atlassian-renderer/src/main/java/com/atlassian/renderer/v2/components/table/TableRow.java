/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 9:12:59 AM
 */
package com.atlassian.renderer.v2.components.table;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class TableRow implements Renderable
{
    List cells;

    public TableRow()
    {
        cells = new LinkedList();
    }

    public static TableRow createRow(String s)
    {
        TableRow row = new TableRow();
        StringBuffer tmp = new StringBuffer();
        boolean isHeader = false;
        char prev = 0;

        char[] chars = s.trim().toCharArray();
        for (int i = 0; i < chars.length; i++)
        {
            char c = chars[i];
            if (c == '|')
            {
                if (i == 0)
                {
                    // do nothing
                }
                else if (prev == '|')
                {
                    isHeader = true;
                }
                else if (prev == '\\')
                {
                    tmp.append(c);
                }
                else // last char was not | or \ - end of cell!
                {
                    row.addCell(new TableCell(tmp.toString(), isHeader));
                    tmp = new StringBuffer();
                    isHeader = false;
                }
            }
            else
            {
                tmp.append(c);
            }

            prev = c;
        }

        if (tmp.length() != 0)
        {
            row.addCell(new TableCell(tmp.toString(), isHeader));
        }

        return row;
    }

    public List getCells()
    {
        return cells;
    }

    public void addCell(TableCell tableCell)
    {
        cells.add(tableCell);
    }

    public void render(SubRenderer subRenderer, RenderContext context, StringBuffer buff)
    {
        buff.append("<tr>\n");
        for (Iterator iterator = cells.iterator(); iterator.hasNext();)
        {
            TableCell tableCell = (TableCell) iterator.next();
            tableCell.render(subRenderer, context, buff);
        }
        buff.append("</tr>\n");
    }

    public String toString()
    {
        StringBuffer result = new StringBuffer("");
        for (Iterator iterator = cells.iterator(); iterator.hasNext();)
        {
            TableCell tableCell = (TableCell) iterator.next();
            result.append("[");
            result.append(tableCell.toString());
            result.append("]");
        }
        return result.toString();
    }

    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof TableRow))
        {
            return false;
        }

        final TableRow tableRow = (TableRow) o;

        if (cells != null ? !cells.equals(tableRow.cells) : tableRow.cells != null)
        {
            return false;
        }

        return true;
    }

    public int hashCode()
    {
        return (cells != null ? cells.hashCode() : 0);
    }
}