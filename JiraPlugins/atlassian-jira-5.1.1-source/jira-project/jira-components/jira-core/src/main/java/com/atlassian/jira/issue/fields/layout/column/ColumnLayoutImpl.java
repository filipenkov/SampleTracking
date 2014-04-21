package com.atlassian.jira.issue.fields.layout.column;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.issue.fields.FieldException;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.fields.layout.AbstractLayout;
import com.atlassian.jira.jql.context.QueryContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public abstract class ColumnLayoutImpl extends AbstractLayout implements ColumnLayout
{
    private List<ColumnLayoutItem> columnLayoutItems;

    public ColumnLayoutImpl(List<ColumnLayoutItem> columnLayoutItems)
    {
        this.columnLayoutItems = columnLayoutItems;
    }

    public List<ColumnLayoutItem> getAllVisibleColumnLayoutItems(final User user) throws ColumnLayoutException
    {
        try
        {
            final Set<NavigableField> availableFields = getFieldManager().getAvailableNavigableFieldsWithScope(user);

            return getVisibleColumnLayoutItems(availableFields);
        }
        catch (FieldException e)
        {
            throw new ColumnLayoutException(e);
        }
    }

    public List<ColumnLayoutItem> getVisibleColumnLayoutItems(final User user, final QueryContext queryContext) throws ColumnLayoutException
    {
        try
        {
            final Set<NavigableField> availableFields = getFieldManager().getAvailableNavigableFieldsWithScope(user, queryContext);

            return getVisibleColumnLayoutItems(availableFields);
        }
        catch (FieldException e)
        {
            throw new ColumnLayoutException(e);
        }
    }

    private List<ColumnLayoutItem> getVisibleColumnLayoutItems(Set<NavigableField> availableFields) throws ColumnLayoutException
    {
        final List<ColumnLayoutItem> visibleColumns = new LinkedList<ColumnLayoutItem>();
        for (ColumnLayoutItem layoutItem : getInternalList())
        {
            if (availableFields.contains(layoutItem.getNavigableField()))
            {
                visibleColumns.add(layoutItem);
            }
        }

        return visibleColumns;
    }

    public boolean contains(NavigableField navigableField)
    {
        for (ColumnLayoutItem columnLayoutItem : getInternalList())
        {
            if (columnLayoutItem.getNavigableField().equals(navigableField))
            {
                return true;
            }
        }
        return false;
    }

    protected List<ColumnLayoutItem> getInternalList()
    {
        return columnLayoutItems;
    }

    protected FieldManager getFieldManager()
    {
        return ComponentManager.getInstance().getFieldManager();
    }
}
