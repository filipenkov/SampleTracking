package com.atlassian.crowd.embedded.admin.list;

/**
 * Position of an item in a list.
 */
public final class ListItemPosition
{
    private final int index;
    private final int totalItems;

    public ListItemPosition(int index, int totalItems)
    {
        this.index = index;
        this.totalItems = totalItems;
    }

    public boolean canMoveUp()
    {
        return index > 0;
    }

    public boolean canMoveDown()
    {
        return index < totalItems - 1;
    }
}
