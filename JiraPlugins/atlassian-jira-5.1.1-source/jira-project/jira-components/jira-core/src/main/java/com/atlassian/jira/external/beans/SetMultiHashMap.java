package com.atlassian.jira.external.beans;

import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.set.ListOrderedSet;

import java.util.Arrays;
import java.util.Collection;
/**
 * A MultiHashMap implementation backed by a HashSet. It also quietly rejects Empty Values strings
 */
public class SetMultiHashMap extends MultiHashMap
{

    public Object put(Object key, Object value)
    {
        if (value instanceof Object[])
        {
            return Boolean.valueOf(this.putAll(key, Arrays.asList((Object[]) value)));
        }
        else if (value instanceof Collection)
        {
            return Boolean.valueOf(this.putAll(key, (Collection) value));
        }
        else if (value != null && !"".equals(value))
        {
            return super.put(key, value);
        }
        else
        {
            return null;
        }
    }

    protected Collection createCollection(Collection coll)
    {
        if (coll == null)
        {

            return new ListOrderedSet();
        }
        else
        {
            final ListOrderedSet listOrderedSet = new ListOrderedSet();
            listOrderedSet.addAll(coll);
            return listOrderedSet;
        }
    }
}
