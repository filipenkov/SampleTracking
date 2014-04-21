/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.query.order.SearchSort;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class SearchRequestUtils
{
    private static final Logger log = Logger.getLogger(SearchRequestUtils.class);

    /**
     * Creates the most specific {@link SearchContext} possible from the two parameters. The baseSearchContext is used as
     * the parent context and the possibleContext is used to narrow it down. Thus if the possibleContext contains Project A
     * and the baseSearchContext is global, a context with Project A will be returned. If baseSearchContext is Project A &amp; Project B
     * the Project A will be returned. If baseSearchContext is only Project B, then Project B will be returned. The same
     * logic applies for issue types.
     *
     * @param baseSearchContext the base <em>parent</em> context
     * @param possibleContext the context to try to narrow the baseSearchContext on
     * @return a combineed {@link SearchContext} object based on the baseSearchContext. Null if baseSearchContext is null
     */
    public static SearchContext getCombinedSearchContext(SearchContext baseSearchContext, SearchContext possibleContext)
    {
        if (baseSearchContext != null)
        {
            if (possibleContext != null)
            {
                // Deal with the projects
                List combinedProjects;
                if (baseSearchContext.isForAnyProjects())
                {
                    combinedProjects = possibleContext.getProjectIds();
                }
                else
                {
                    combinedProjects = ListUtils.intersection(baseSearchContext.getProjectIds(), possibleContext.getProjectIds() != null ? possibleContext.getProjectIds() : Collections.EMPTY_LIST);
                    if (combinedProjects.isEmpty())
                    {
                        combinedProjects = baseSearchContext.getProjectIds();
                    }
                }

                // Deal with the issue types
                List combinedIssuetypes;
                if (baseSearchContext.isForAnyIssueTypes())
                {
                    combinedIssuetypes = possibleContext.getIssueTypeIds();
                }
                else
                {
                    combinedIssuetypes = ListUtils.intersection(baseSearchContext.getIssueTypeIds(), possibleContext.getIssueTypeIds() != null ? possibleContext.getIssueTypeIds() : Collections.EMPTY_LIST);
                    if (combinedIssuetypes.isEmpty())
                    {
                        combinedIssuetypes = baseSearchContext.getIssueTypeIds();
                    }
                }

                return new SearchContextImpl(null, combinedProjects, combinedIssuetypes);

            }
            else
            {
                return new SearchContextImpl(baseSearchContext);
            }
        }
        else
        {
            return null;
        }
    }

    /**
     * Returns a list of the descriptions of each sorter defined in the search request.
     *
     * If one of the sorters references a field which does not exist, it will be skipped.
     *
     * @param searchRequest the search request containing the sorts; must not be null.
     * @param fieldManager field manager
     * @param searchHandlerManager search handler manager
     * @param searchSortUtil search sort utility
     * @param i18nHelper i18n helper
     * @param searcher the user making the request
     * @return a list of strings describing the sorters; never null.
     * @since v3.13.3
     */
    public static List<String> getSearchSortDescriptions(SearchRequest searchRequest, final FieldManager fieldManager, final SearchHandlerManager searchHandlerManager, final SearchSortUtil searchSortUtil, final I18nHelper i18nHelper, final User searcher)
    {
        Assertions.notNull("searchRequest", searchRequest);
        List<String> searchSortDescriptions = new ArrayList<String>();

        final List<SearchSort> searchSorts = searchSortUtil.getSearchSorts(searchRequest.getQuery());
        for (Iterator iterator = searchSorts.iterator(); iterator.hasNext();)
        {
            SearchSort searchSort = (SearchSort) iterator.next();
            final String sortClauseName = searchSort.getField();

            final List<String> fieldIds = new ArrayList<String>(searchHandlerManager.getFieldIds(searcher, sortClauseName));
            // sort to get consistent ordering of fields for clauses with multiple fields
            Collections.sort(fieldIds);

            for (String fieldId : fieldIds)
            {
                Field field = fieldManager.getField(fieldId);
                if (field != null)
                {
                    StringBuffer description = new StringBuffer();
                    description.append(i18nHelper.getText(field.getNameKey()));
                    final String orderDescription = getSearchSortOrderDescription(searchSort.getOrder(), field, i18nHelper);
                    if (!StringUtils.isBlank(orderDescription))
                    {
                        description.append(" ").append(orderDescription);
                    }
                    searchSortDescriptions.add(description.toString());
                }
                else
                {
                    log.info("Field '" + sortClauseName + "' is invalid as a search sort in SearchRequest " + searchRequest);
                }
            }
        }

        // now we know that every element in the list is valid, add in the ", then" to all but the last string
        for (int i = 0; i < searchSortDescriptions.size(); i++)
        {
            String description = searchSortDescriptions.get(i);
            if (i < searchSortDescriptions.size() - 1)
            {
                String newDescription = description + ", " + i18nHelper.getText("navigator.hidden.sortby.then");
                searchSortDescriptions.set(i, newDescription);
            }
        }
        return searchSortDescriptions;
    }

    private static String getSearchSortOrderDescription(String searchSortOrder, final Field field, final I18nHelper i18nHelper)
    {
        if (!(field instanceof NavigableField))
        {
            return "";
        }

        NavigableField navigableField = (NavigableField) field;

        searchSortOrder = StringUtils.isBlank(searchSortOrder) ? navigableField.getDefaultSortOrder() : searchSortOrder;
        if (NavigableField.ORDER_DESCENDING.equals(searchSortOrder))
        {
            return i18nHelper.getText("navigator.hidden.sortby.descending");
        }
        else
        {
            return i18nHelper.getText("navigator.hidden.sortby.ascending");
        }
    }

}
