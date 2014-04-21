package com.atlassian.renderer.v2.components.block;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.Stack;

public class LineWalker
{
    private BufferedReader reader;
    private String nextLine;
    private Stack pushedBackLines = new Stack();

    public LineWalker(String text)
    {
        reader = new BufferedReader(new StringReader(text));
    }

    public boolean hasNext()
    {
        try
        {
            if (nextLine == null && pushedBackLines.empty())
                nextLine = reader.readLine();

            return nextLine != null || !pushedBackLines.empty();
        }
        catch (IOException e)
        {
            throw new RuntimeException("IO Exception reading from string: " + e.getMessage(), e);
        }
    }

    public String peek()
    {
        if (!hasNext())
            throw new IllegalStateException("No more lines");

        if (!pushedBackLines.empty())
            return (String) pushedBackLines.peek();

        return nextLine;
    }

    public String next()
    {
        if (!hasNext())
            throw new IllegalStateException("No more lines");

        if (!pushedBackLines.empty())
            return (String) pushedBackLines.pop();

        String retval = nextLine;
        nextLine = null;
        return retval;
    }

    public void pushBack(String line)
    {
        pushedBackLines.push(line);
    }
}
