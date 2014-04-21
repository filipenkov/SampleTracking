package com.atlassian.gadgets.dashboard.internal;

import java.io.StringWriter;

import com.atlassian.gadgets.view.ViewComponent;

public abstract class AbstractViewComponent implements ViewComponent
{
    private final String id;
    private final String title;

    protected AbstractViewComponent(String id, String title)
    {
        this.id = id;
        this.title = title;
    }

    public String getId()
    {
        return id;
    }

    public String getTitle()
    {
        return title;
    }

    @Override
    public String toString()
    {
        try
        {
            StringWriter writer = new StringWriter();
            writeTo(writer);
            return writer.toString();
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
}
