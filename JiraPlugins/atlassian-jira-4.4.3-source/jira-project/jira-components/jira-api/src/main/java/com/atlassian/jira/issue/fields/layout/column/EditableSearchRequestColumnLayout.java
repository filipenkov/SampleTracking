/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.jira.issue.search.SearchRequest;

public interface EditableSearchRequestColumnLayout extends EditableUserColumnLayout
{
    public SearchRequest getSearchRequest();

    public void setSearchRequest(SearchRequest searchRequest);
}
