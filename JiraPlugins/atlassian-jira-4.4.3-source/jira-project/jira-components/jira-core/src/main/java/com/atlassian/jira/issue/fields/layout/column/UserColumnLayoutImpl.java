/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.FieldManager;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class UserColumnLayoutImpl extends ColumnLayoutImpl implements UserColumnLayout
{
    private final User user;

    public UserColumnLayoutImpl(List columnLayoutItems, User user)
    {
        super(columnLayoutItems);
        this.user = user;
    }

    public List getColumnLayoutItems()
    {
        FieldManager fieldManager = getFieldManager();

        // Return the internal list minus the project custom fields the user can not see.
        List columnLayoutItems = new ArrayList();
        for (Iterator iterator = getInternalList().iterator(); iterator.hasNext();)
        {
            ColumnLayoutItem columnLayoutItem = (ColumnLayoutItem) iterator.next();
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

    public User getUser()
    {
        return user;
    }
}
