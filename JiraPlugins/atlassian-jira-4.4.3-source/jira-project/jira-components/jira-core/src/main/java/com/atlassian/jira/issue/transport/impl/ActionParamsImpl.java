package com.atlassian.jira.issue.transport.impl;

import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.util.JiraCollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ActionParamsImpl implements ActionParams
{
    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    protected Map params;

    // ---------------------------------------------------------------------------------------------------- Constructors
    public ActionParamsImpl()
    {
        params = new HashMap();
    }

    public ActionParamsImpl(Map params)
    {
        validateMap(params);
        this.params = new HashMap(params);
    }

    // --------------------------------------------------------------------------------------------- FieldParams Methods
    public Set getAllKeys()
    {
        return params.keySet();
    }

    public Map getKeysAndValues()
    {
        return new HashMap(params);
    }

    public boolean containsKey(String key)
    {
        return params.containsKey(key);
    }

    public boolean isEmpty()
    {
        return params.isEmpty();
    }

    // ---------------------------------------------------------------------------------------- CollectionParams methods
    public String[] getAllValues()
    {
        List allValues = new ArrayList();
        for (Iterator iterator = params.values().iterator(); iterator.hasNext();)
        {
            final String[] array = (String[]) iterator.next();
            allValues.addAll(Arrays.asList(array));
        }

        String[] returnValue = JiraCollectionUtils.stringCollectionToStringArray(allValues);

        return returnValue;
    }

    public String[] getValuesForNullKey()
    {
        return getValuesForKey(null);
    }

    public String[] getValuesForKey(String key)
    {
        return (String[]) params.get(key);
    }

    // -------------------------------------------------------------------------------------------- StringParams methods
    public String getFirstValueForNullKey()
    {
        return getFirstValueForKey(null);
    }

    public String getFirstValueForKey(String key)
    {
        String[] c = getValuesForKey(key);

        if (c != null && c.length > 0)
        {
            return (String) c[0];
        }
        else
        {
            return null;
        }
    }

    public void put(String id, String[] values)
    {
        params.put(id,  values);
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
    /**
     * Ensures that the map has Collection of Strings as its value
     *
     * @param params
     */
    private void validateMap(Map params)
    {
        // @todo implement this
    }

}
