/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.jira.issue.fields.NavigableField;

public interface EditableColumnLayout extends ColumnLayout
{
    public void addColumn(NavigableField navigableField);

    public void removeColumn(ColumnLayoutItem columnLayoutItem);

    public void moveColumnLeft(ColumnLayoutItem columnLayoutItem);

    public void moveColumnRight(ColumnLayoutItem columnLayoutItem);
}
