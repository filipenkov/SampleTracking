package com.atlassian.jira.util;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Utility methods that work on Maps.
 *
 * @since v4.3
 */
public class MapUtils
{
    public static MultiMap invertMap(final Map mapToInvert)
    {
        final MultiMap invertedMap = new MultiHashMap();
        final Set entries = mapToInvert.entrySet();
        for (final Iterator iterator = entries.iterator(); iterator.hasNext();)
        {
            final Map.Entry entry = (Map.Entry) iterator.next();
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            invertedMap.put(value, key);
        }
        return invertedMap;
    }
}
