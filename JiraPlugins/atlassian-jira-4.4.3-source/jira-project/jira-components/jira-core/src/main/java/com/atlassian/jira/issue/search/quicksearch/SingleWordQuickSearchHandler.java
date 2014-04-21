package com.atlassian.jira.issue.search.quicksearch;

import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

public abstract class SingleWordQuickSearchHandler implements QuickSearchHandler
{
    public void modifySearchResult(QuickSearchResult searchResult)
    {
        String searchString = searchResult.getSearchInput();
        StringTokenizer st = new StringTokenizer(searchString, " ");

        while (st.hasMoreTokens())
        {
            String word = st.nextToken();
            Map params = handleWord(word, searchResult);
            if (params != null)
            {
                String searchWithoutTypeName = strip(searchString, word).trim();
                searchResult.setSearchInput(searchWithoutTypeName);
                for (Iterator iterator = params.entrySet().iterator(); iterator.hasNext();)
                {
                    Map.Entry entry = (Map.Entry) iterator.next();
                    Object value = entry.getValue();
                    if (value instanceof Collection)
                    {
                        Collection values = (Collection)entry.getValue();
                        for (Iterator iterator1 = values.iterator(); iterator1.hasNext();)
                        {
                            String singleValue = (String) iterator1.next();
                            searchResult.addSearchParameter((String) entry.getKey(), singleValue);
                        }

                    }
                    else if (value instanceof String)
                    {
                        searchResult.addSearchParameter((String) entry.getKey(), (String) value);
                    }
                }
                break;
            }
        }

    }

    /**
     * Hanldle a single word, and return a map of parameters
     * @param word  A single word - guaranteed to not be null
     * @param searchResult  The existing search result
     * @return  A Map of parameters that can be used as a URL.
     */
    protected abstract Map handleWord(String word, QuickSearchResult searchResult);

    /**
     * Strip a string from another string.
     */
    private static String strip(String originalString, String stringToRemove)
    {
        final int index = originalString.indexOf(stringToRemove);
        if (index == -1)
            return originalString;
        else
            return originalString.substring(0, index) + originalString.substring(index + stringToRemove.length());
    }

    /**
     * For a Collection of Generic Values, return the one that has
     * <code>genericValue.getString("name")</code> equal to <code>name</code>
     */
    protected static GenericValue getByName(final Collection genericValues, String name)
    {
        for (Iterator iterator = genericValues.iterator(); iterator.hasNext();)
        {
            GenericValue genericValue = (GenericValue) iterator.next();
            if (name.equalsIgnoreCase(genericValue.getString("name")))
                return genericValue;
        }
        return null;
    }

}
