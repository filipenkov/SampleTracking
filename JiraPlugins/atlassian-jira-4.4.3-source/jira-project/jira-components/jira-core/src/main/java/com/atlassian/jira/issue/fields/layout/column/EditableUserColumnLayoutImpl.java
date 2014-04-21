/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ManagerFactory;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public class EditableUserColumnLayoutImpl extends EditableColumnLayoutImpl implements EditableUserColumnLayout
{
    private final User user;

    public EditableUserColumnLayoutImpl(List columnLayoutItems, User user)
    {
        // Ensure the Layout List is editable
        super(new ArrayList(columnLayoutItems));
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");
        this.user = user;
    }

    public User getUser()
    {
        return user;
    }

    public List<ColumnLayoutItem> getColumnLayoutItems()
    {
        FieldManager fieldManager = ManagerFactory.getFieldManager();

        // Return the internal list minus the project custom fields the user can not see.
        List<ColumnLayoutItem> columnLayoutItems = new LinkedList<ColumnLayoutItem>();
        for (final ColumnLayoutItem columnLayoutItem : getInternalList())
        {
            if (fieldManager.isCustomField(columnLayoutItem.getNavigableField()))
            {
                CustomField customField = (CustomField) columnLayoutItem.getNavigableField();
                if (CustomFieldUtils.isUserHasPermissionToProjects(customField, getUser()))
                {
                    columnLayoutItems.add(columnLayoutItem);
                }
            }
            else
            {
                columnLayoutItems.add(columnLayoutItem);
            }
        }
        return columnLayoutItems;
    }
}
