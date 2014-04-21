/*
 * Created by IntelliJ IDEA.
 * User: mike
 * Date: Jan 22, 2002
 * Time: 11:31:29 AM
 * To change template for new class use
 * Code Style | Class Templates options (Tools | IDE Options).
 */
package com.atlassian.core.util.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * This is a super class that implements paging for browsers.
 *
 * Most other filters (which want paging ability) will extend this.
 */
public class PagerFilter implements Serializable
{
    int max = 20;
    int start = 0;

    /**
     * Get the current page out of a list of items - won't work well on any collection that isn't a list
     */
    public Collection getCurrentPage(Collection itemsCol)
    {
        List items = null;

        if (itemsCol instanceof List)
            items = (List) itemsCol;
        else
            items = new ArrayList(itemsCol); // should never call this but just incase!

        if (items == null || items.size() == 0)
        {
            start = 0;
            return Collections.EMPTY_LIST;
        }

        // now return the appropriate page of issues
        // now make sure that the start is valid
        if (start > items.size())
        {
            start = 0;
            return items.subList(0, max);
        }
        else
        {
            return items.subList(start, Math.min(start + max, items.size()));
        }
    }

    public int getMax()
    {
        return max;
    }

    public void setMax(int max)
    {
        this.max = max;
    }

    public int getStart()
    {
        return start;
    }

    public void setStart(int start)
    {
        this.start = start;
    }

    public int getEnd()
    {
        return start + max;
    }

    public int getNextStart()
    {
        return start + max;
    }

    public int getPreviousStart()
    {
        return Math.max(0, start - max);
    }
}
