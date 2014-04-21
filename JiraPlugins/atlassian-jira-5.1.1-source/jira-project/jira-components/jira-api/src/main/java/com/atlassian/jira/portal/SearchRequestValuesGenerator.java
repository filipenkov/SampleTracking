package com.atlassian.jira.portal;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

/**
 * A ValuesGenerator for creating a list of SearchRequests (a.k.a Filters) with IDs and names.
 * The filterpicker Configurable Objects type should be used instead of a select and this ValuesGenerator since the
 * number of filters returned could be too large for a select control.
 */
public class SearchRequestValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(SearchRequestValuesGenerator.class);

    /**
     * Returns a map of the filter id to the filter name, requiring only the "User" parameter in the params map.
     */
    public Map getValues(Map params)
    {
        Map savedFilters = null;
        User u = (User) params.get("User");

        Collection savedFiltersList = ComponentAccessor.getComponent(SearchRequestService.class).getFavouriteFilters(u);
        savedFilters = new ListOrderedMap();

        for (Iterator iterator = savedFiltersList.iterator(); iterator.hasNext();)
        {
            SearchRequest request = (SearchRequest) iterator.next();
            savedFilters.put(request.getId().toString(), request.getName());
        }


        return savedFilters;
    }
}
