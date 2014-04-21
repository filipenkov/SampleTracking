/*
 * Created by IntelliJ IDEA.
 * User: Mike
 * Date: Nov 15, 2004
 * Time: 9:10:16 AM
 */
package com.atlassian.renderer.v2.components.table;

import com.atlassian.renderer.RenderContext;
import com.atlassian.renderer.v2.Renderable;
import com.atlassian.renderer.v2.SubRenderer;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

public class Table implements Renderable
{
    List rows;

    public Table()
    {
        rows = new LinkedList();
    }

    public void addRow(String s)
    {
        addRow(TableRow.createRow(s));
    }

    public void addRow(TableRow row)
    {
        rows.add(row);
    }

    public void render(SubRenderer subRenderer, RenderContext context, StringBuffer buff)
    {
        buff.append("<table class='confluenceTable'><tbody>\n");
        for (Iterator iterator = rows.iterator(); iterator.hasNext();)
        {
            TableRow tableRow = (TableRow) iterator.next();
            tableRow.render(subRenderer, context, buff);
        }
        buff.append("</tbody></table>");
    }

    public static Table createTable(String raw)
    {
        Table t = new Table();
        StringTokenizer st = new StringTokenizer(raw, "\n");

        if (st.countTokens() > 0)
        {
            String currentRow = st.nextToken();

            while (st.hasMoreTokens())
            {
                String s = st.nextToken();

                if (s.charAt(0) == '|')
                {
                    t.addRow(currentRow);
                    currentRow = s;
                }
                else
                {
                    currentRow += "\n" + s;
                }
            }

            t.addRow(currentRow);
        }

        return t;
    }

    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof Table)) return false;

        final Table table = (Table) o;

        if (rows != null ? !rows.equals(table.rows) : table.rows != null) return false;

        return true;
    }

    public int hashCode()
    {
        return (rows != null ? rows.hashCode() : 0);
    }
}