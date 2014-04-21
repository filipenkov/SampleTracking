package com.atlassian.crowd.search.hibernate;

import java.util.ArrayList;
import java.util.List;

public class HibernateSearchResultsTransformer
{
    public static List transformResults(List results)
    {
        List newResults = new ArrayList(results.size());
        for (Object object : results)
        {
            if (object instanceof Object[] && ((Object[]) object).length > 0)
            {
                newResults.add(((Object[]) object)[0]);
            }
            else
            {
                newResults.add(object);
            }
        }
        return newResults;
    }
}
