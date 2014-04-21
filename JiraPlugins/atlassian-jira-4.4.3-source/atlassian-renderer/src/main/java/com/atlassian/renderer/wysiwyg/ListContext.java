package com.atlassian.renderer.wysiwyg;



/**
 * Created by IntelliJ IDEA.
 * User: Tomd
 * Date: 9/06/2005
 * Time: 17:38:22
 * To change this template use File | Settings | File Templates.
 */
public class ListContext
{
    public static final String NUMBERED = "#";
    public static final String BULLETED = "*";
    public static final String SQUARE = "-";

    private String stack = "";

    public ListContext(String type, ListContext current)
    {
        stack = current.getStack() + type;
    }

    public ListContext()
    {
    }

    public ListContext(String type) {
        stack = type;
    }

    public String getStack()
    {
        return stack;
    }

    public String decorateText(String s)
    {
        return stack + " " + s;
    }

    public boolean isInList()
    {
        return stack.length() > 0;
    }
}
