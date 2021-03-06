/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;

public interface UserColumnLayout extends ColumnLayout
{
    User getUser();
}
