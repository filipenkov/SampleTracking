/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue.bulkedit;

import com.atlassian.jira.bc.issue.search.SearchService;

public abstract class AbstractBulkOperationDetailsAction extends AbstractBulkOperationAction
{
    public AbstractBulkOperationDetailsAction(SearchService searchService)
    {
        super(searchService);
    }

    public abstract String doDetails() throws Exception;

    public abstract String doDetailsValidation() throws Exception;

    public abstract String doPerform() throws Exception;

}
