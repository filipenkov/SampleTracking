package com.atlassian.jira.issue.operation;

import com.atlassian.annotations.PublicApi;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface IssueOperation
{
    String getNameKey();

    String getDescriptionKey();
}
