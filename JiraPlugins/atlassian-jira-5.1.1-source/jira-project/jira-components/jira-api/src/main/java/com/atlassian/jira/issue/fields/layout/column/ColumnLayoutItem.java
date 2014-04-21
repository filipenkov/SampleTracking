/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.annotations.PublicApi;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.NavigableField;

import java.util.Map;

@PublicApi
public interface ColumnLayoutItem extends Comparable
{
    NavigableField getNavigableField();

    boolean isAliasForField(User user, String sortField);

    int getPosition();

    String getHtml(Map displayParams, Issue issue);

    /**
     * Return some text for the Column Header.  By default this calls
     * {@link NavigableField#getColumnHeadingKey} but implementations can
     * override this to provide different column headings as appropriate
     *
     * @return  A key, which can be run through {@link com.atlassian.jira.util.I18nHelper#getText(String)} to get a heading
     */
    String getColumnHeadingKey();

}
