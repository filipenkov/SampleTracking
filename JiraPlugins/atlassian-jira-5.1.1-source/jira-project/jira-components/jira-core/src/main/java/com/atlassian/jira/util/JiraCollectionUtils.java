package com.atlassian.jira.util;

import java.util.Collection;
import java.util.Iterator;

public class JiraCollectionUtils
{

    public static String[] stringCollectionToStringArray(Collection allValues)
    {
        String[] returnValue = new String[allValues.size()];
        int i = 0;
        for (Iterator iterator = allValues.iterator(); iterator.hasNext();)
        {
            String s = (String) iterator.next();
            returnValue[i] = s;
            i++;
        }
        return returnValue;
    }
}
