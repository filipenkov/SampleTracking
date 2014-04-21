package com.atlassian.plugins.rest.common.expand.entity;

import com.atlassian.plugins.rest.common.expand.parameter.Indexes;
import com.google.common.collect.Lists;

import java.util.List;

public class ListWrapperCallBacks
{
    public static <T> ListWrapperCallback<T> identity(final List<T> items)
    {
        return new ListWrapperCallback<T>()
        {
            public List<T> getItems(Indexes indexes)
            {
                return items;
            }
        };
    }

    public static <T> ListWrapperCallback<T> ofList(final List<T> items)
    {
        return ofList(items, Integer.MAX_VALUE);
    }

    public static <T> ListWrapperCallback<T> ofList(final List<T> items, final int maxResults)
    {
        return new ListWrapperCallback<T>()
        {
            public List<T> getItems(Indexes indexes)
            {
                final List<T> toReturn = Lists.newLinkedList();
                for (Integer i : indexes.getIndexes(items.size()))
                {
                    if (i < items.size())
                    {
                        toReturn.add(items.get(i));
                    }
                    if (toReturn.size() == maxResults)
                    {
                        break;
                    }
                }
                return toReturn;
            }
        };
    }
}
