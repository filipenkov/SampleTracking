package com.atlassian.crowd.search.util;

import com.atlassian.crowd.model.DirectoryEntity;
import com.atlassian.crowd.search.query.entity.EntityQuery;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for processing search results.
 */
public class SearchResultsUtil
{
    /**
     * Transforms collection of directory entities to collection of entity names.
     *
     * @param entities directory entities.
     * @return names.
     */
    public static List<String> convertEntitiesToNames(List<? extends DirectoryEntity> entities)
    {
        List<String> names = new ArrayList<String>(entities.size());
        for (DirectoryEntity entity : entities)
        {
            names.add(entity.getName());
        }

        return names;
    }

    /**
     * Performs a sublist operation on the list of results.
     *
     * @param results collection of all results.
     * @param startIndex start index for output.
     * @param maxResults max number of results for output. The special value {@code EntityQuery.ALL_RESULTS} is honoured.
     * @param <T> type of results is unchanged.
     * @return output sublist.
     */
    public static <T> List<T> constrainResults(List<T> results, int startIndex, int maxResults)
    {
        int endIndex = startIndex + maxResults;
        if (endIndex > results.size() || maxResults == EntityQuery.ALL_RESULTS)
        {
            endIndex = results.size();
        }

        // Need to make sure that start-index isn't past the end of the array or an IllegalArgumentException will be '
        //  thrown => adjust to equal end so no results are returned.
        if (startIndex > endIndex)
        {
            startIndex = endIndex;
        }

        return results.subList(startIndex, endIndex);
    }
}
