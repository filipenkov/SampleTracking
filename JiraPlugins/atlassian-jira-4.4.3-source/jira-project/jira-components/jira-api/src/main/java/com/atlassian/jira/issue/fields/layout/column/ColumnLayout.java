/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.jql.context.QueryContext;

import java.util.List;

public interface ColumnLayout
{
    public List<ColumnLayoutItem> getColumnLayoutItems();

    /**
     * Get the {@link com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem ColumnLayoutItems} that can be displayed to the user.
     *
     * @param user       the user for whom the columns are to be displayed.
     * @param queryContext the context of the search the columns are being displayed for
     * @return All visible column layout items for the given query context
     * @throws ColumnLayoutException if exception thrown while retreiving column layout
     */
    public List<ColumnLayoutItem> getVisibleColumnLayoutItems(User user, QueryContext queryContext) throws ColumnLayoutException;


    /**
     * Get the {@link com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem ColumnLayoutItems} that can be displayed to the user.
     *
     * @param user       the user for whom the columns are to be displayed.
     * @return All visible column layout items
     * @throws ColumnLayoutException if exception thrown while retreiving column layout
     */
    public List<ColumnLayoutItem> getAllVisibleColumnLayoutItems(User user) throws ColumnLayoutException;

    public boolean contains(NavigableField navigableField);
}
